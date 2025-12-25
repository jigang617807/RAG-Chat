package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import com.example.demo.entity.Document;
import com.example.demo.service.DocumentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.example.demo.utils.TextSplitter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService docService;
    @Value("${upload.dir}")
    private String uploadDir;   // D:/javalearning/demo

    @Value("${upload.docs}")
    private String docsDir;   // uploads/docs/



    // ----------------- ① 上传页面入口 -----------------
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";   // 返回 upload.html
    }

    // ----------------- ② 上传 PDF 文件 -----------------
    @PostMapping("/upload")
    public String uploadPdf(@RequestParam("file") MultipartFile file,
                            HttpSession session) throws Exception {

        Long uid = (Long) session.getAttribute("uid");

        String filename = "u" + uid + "_" + file.getOriginalFilename();

        //String path = uploadDir + "/" + docsDir + filename;

        // 如果 uploadDir 是 "."，这里会得到类似 "D:\javalearning\demo" 的路径
        String absolutePath = new File(uploadDir).getAbsolutePath();
        String path = absolutePath + "/" + docsDir + filename;


        File dest = new File(path);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);

        // 解析 PDF
        PDDocument pdf = PDDocument.load(dest);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdf);
        pdf.close();

        // 简单切分 chunk（两段换行）
        //List<String> chunks = Arrays.asList(text.split("\n\n"));
        List<String> chunks = TextSplitter.splitText(text, 800, 200);




        // 保存文档信息
        Document doc = docService.saveDocument(uid, filename, path, text);

        // 保存 chunk
        docService.saveChunks(doc.getId(), chunks);

        return "redirect:/doc/list";
    }

    // ----------------- ③ 文档列表页面 -----------------
    @GetMapping("/list")
    public String listDocs(HttpSession session, Model model) {

        Long uid = (Long) session.getAttribute("uid");

        // 未登录情况处理
        if (uid == null) return "redirect:/auth/login";

        // 查询当前用户的所有文档
        List<Document> docs = docService.listDocs(uid);

        model.addAttribute("docs", docs);
        return "doc_list";   // 返回 doc_list.html
    }

    //申请删除
    @PostMapping("/delete")
    public ResponseEntity<String> deleteDocumentAndRelatedData(@RequestParam("docId") Long docId) {
        // 1. 参数校验
        if (docId == null) {
            // 返回 400 Bad Request 状态码
            return ResponseEntity.badRequest().body("文档ID不能为空。");
        }
        try {
            // 2. 调用 Service 层处理业务逻辑（包含事务、多表删除和本地文件删除）
            docService.deleteDocumentWithRelatedData(docId);

            // 3. 成功响应
            // 返回 200 OK 状态码，前端的 .then(response => { if (response.ok) ... }) 将会捕获到
            return ResponseEntity.ok("文档删除成功");

        } catch (RuntimeException e) {
            // 捕获 Service 层抛出的业务异常（如“文档不存在”）
            // 建议在这里使用 Logger 记录详细错误堆栈
            // logger.error("删除文档 ID: {} 时出现业务或文件错误。", docId, e);

            // 4. 失败响应（返回 500 Internal Server Error）
            return ResponseEntity.internalServerError().body("删除失败：" + e.getMessage());

        } catch (Exception e) {
            // 捕获其他不可预见的系统级异常
            // logger.error("删除文档 ID: {} 时出现系统错误。", docId, e);
            return ResponseEntity.internalServerError().body("服务器处理请求时发生未知错误。");
        }
    }



}
