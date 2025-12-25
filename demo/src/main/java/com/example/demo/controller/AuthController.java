package com.example.demo.controller;
//（注册 / 登录 / 忘记密码 / home）
//路由前缀：/auth
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private Map<String, String> smsCodes = new HashMap<>();

    // ---------------------- 页面入口 ----------------------

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/reset")
    public String resetPage() { return "reset_password"; }


    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        Object username = session.getAttribute("username");
        Object avatarPath = session.getAttribute("avatarPath");
        Long uid = (Long) session.getAttribute("uid");
        User user = userService.getUser(uid);

        if (username == null) return "redirect:/auth/login";
        model.addAttribute("username", username);
        model.addAttribute("avatarPath", avatarPath);
        return "home";
    }

    // ----------------------- 提交接口 -----------------------

    @PostMapping("/register-page")
    public String registerSubmit(User user, Model model) {

        String msg = userService.register(user);

        if (!"注册成功".equals(msg)) {
            model.addAttribute("error", msg);
            return "register";
        }

        model.addAttribute("success", "注册成功，请登录");
        return "login";
    }

    @PostMapping("/login-page")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              Model model,
                              HttpSession session) {

        User user = userService.loginAndReturnUser(username, password);

        if (user == null) {
            model.addAttribute("error", "用户名或密码错误");

            return "login";
        }

        // 存入 Session
        System.out.println(user.getId());
        System.out.println(user.getUsername());
        System.out.println(user.getAvatarPath());
        session.setAttribute("uid", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("avatarPath", user.getAvatarPath());
        return "redirect:/auth/home";
    }


    // ----------------------- 忘记密码 -----------------------

    @GetMapping("/sms")
    @ResponseBody
    public String sendSms(@RequestParam String phone,Model model) {
        if (phone == null || phone.trim().isEmpty()){ // 使用更健壮的检查
            return "请先输入电话号再获取验证码";
        }

        // --- 核心修改：增加手机号注册校验 ---
        // 注意：findByPhone 返回 Optional<User>
        if (userService.findByPhone(phone).isEmpty()) {
            // 【安全最佳实践】即使手机号未注册，也返回一个通用的成功/已发送信息，
            // 防止泄露系统中哪些手机号是注册用户。
            System.out.println("手机号未注册，不发送验证码。返回通用信息。");
            return "验证码已发送（模拟）";
        }
        // 只有通过校验的手机号才会执行到这里
        String code = (new Random().nextInt(900000) + 100000) + "";
        smsCodes.put(phone, code);
        System.out.println("模拟验证码：" + code);
        return "验证码已发送（模拟）";
    }


    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam String phone,
                              @RequestParam String code,
                              @RequestParam String newPassword,
                              Model model) {

        if (!smsCodes.containsKey(phone)) {
            model.addAttribute("error", "请先获取验证码");
            return "reset_password";
        }

        if (!smsCodes.get(phone).equals(code)) {
            model.addAttribute("error", "验证码错误");
            return "reset_password";
        }

        String msg = userService.resetPassword(phone, newPassword);
        if (!"密码重置成功".equals(msg)) {
            model.addAttribute("error", msg);
            return "reset_password";
        }


        model.addAttribute("success", "密码重置成功，请登录");
        //防止输入同一个 phone，会匹配旧的 code 或混乱
        smsCodes.remove(phone);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}

