package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${upload.dir}")
    private String uploadDir;   // D:/javalearning/demo

    @Value("${upload.avatar}")
    private String avatarDir;   // uploads/avatar/

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        Long uid = (Long) session.getAttribute("uid");
        User user = userService.getUser(uid);

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update")
    public String update(User form, HttpSession session) {

        Long uid = (Long) session.getAttribute("uid");
        form.setId(uid);

        userService.updateUser(form);
        return "redirect:/user/profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) throws Exception {

        Long uid = (Long) session.getAttribute("uid");

        String filename = "u" + uid + "_" + file.getOriginalFilename();

        // 构造最终全路径：D:/javalearning/demo/uploads/avatar/xxxx.jpg
        //String fullPath = uploadDir + "/" + avatarDir + filename;


        // 如果 uploadDir 是 "."，这里会得到类似 "D:\javalearning\demo" 的路径
        String absolutePath = new File(uploadDir).getAbsolutePath();
        String fullPath = absolutePath + "/" + avatarDir + filename;

        File dest = new File(fullPath);
        dest.getParentFile().mkdirs(); // ⭐ 确保目录存在

        file.transferTo(dest);         // ⭐ 保存文件

        // 数据库存相对路径：/uploads/avatar/xxxx.jpg
        userService.updateAvatar(uid, "/" + avatarDir + filename);

        String webPath = "/" + avatarDir + filename;
        //同步更新 Session 中的头像，防止其他页面读取到旧数据
        session.setAttribute("avatarPath", webPath);

        return "redirect:/user/profile";
    }
}
