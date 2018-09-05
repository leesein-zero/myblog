package com.liyang.blog.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.liyang.blog.pojo.Article;
import com.liyang.blog.pojo.HostHolder;
import com.liyang.blog.pojo.User;
import com.liyang.blog.pojo.Item;
import com.liyang.blog.service.ArticleService;
import com.liyang.blog.service.TagService;
import com.liyang.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class indexController {
    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = {"/","/index"})
    public String index(Model model,
                        @RequestParam(value = "pageNumber",required = false,defaultValue = "1")int pageNumber){
        PageHelper.startPage(pageNumber,4);
        List<Item> wholeArticle = new ArrayList<>();
        List<Article> list = articleService.findAllArticle(); //article List
//        ArrayList<Object> articleTags = new ArrayList<>();  //每个article一个元素，内含tagname List
//        for (Article article:list){
//            List<String> tagByArticleId = tagService.getTagByArticleId(article.getId());
//            articleTags.add(tagByArticleId);
//        }

        for (Article article:list){
            List<String> tagByArticleId = tagService.getTagByArticleId(article.getId());
            Item item = new Item();
            item.set("article",article);
            item.set("tagName",tagByArticleId);
            wholeArticle.add(item);
        }

        PageInfo<Article> pageInfo = new PageInfo<Article>(list);
        //文章列表
//        model.addAttribute("articles",list);
        model.addAttribute("wholeArticle",wholeArticle);
        model.addAttribute("pageInfo",pageInfo);
        User user = null;
        try {
            user = (User) hostHolder.getUser().get("user");
        } catch (Exception e) {
            user=null;
        }

        if (user!=null){
            if("admin".equals(user.getRole())){//user非空，且是admin用户
                model.addAttribute("create",1);
            }else {
                //已登录，不过是普通用户
                model.addAttribute("create",0);
            }

        }else {//user==null,那就是没有登录注册过
            model.addAttribute("create",0);
        }
        return "index";
    }

    //访问登录页面
    @RequestMapping("/in")
    public String in(){
        return "login";
    }


//    //注册请求
//    @RequestMapping("/register")
//    public String register(String username,
//                        String password,
//                        Model model,
//                        HttpServletResponse httpResponse){
//        Map map = userService.register(username,password);
//        if (map.containsKey("ticket")){
//            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
//            cookie.setPath("/");
//            httpResponse.addCookie(cookie);
//
//            return "redirect:/";
//        }else {
//            model.addAttribute("msg",map.get("msg"));
//            return "login";
//        }
//    }


    //注册请求
    @RequestMapping("/register")
    public String register(@Valid User user,
                           BindingResult bindingResult,
                           Model model,
                           HttpServletResponse httpResponse){
        //字段验证
        if(bindingResult.hasErrors()){  //信息输入有错
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError error : fieldErrors) {
                model.addAttribute((String) error.getField(),(String)error.getDefaultMessage());
            }
            return "login";
        }
        Map map = userService.register(user.getName(),user.getPassword());
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath("/");
            httpResponse.addCookie(cookie);

            return "redirect:/";
        }else {
            model.addAttribute("msg",map.get("msg"));
            return "login";
        }
    }

    //点击登录按钮的请求
    @RequestMapping("/login")
    public String login(Model model, HttpServletResponse httpResponse,
                        User user){
        Map map = userService.login(user.getName(),user.getPassword());
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath("/");
            httpResponse.addCookie(cookie);

            return "redirect:/";
        }else {
            model.addAttribute("msg", map.get("msg"));
            return "login";
        }

    }

    //添加文章
    @RequestMapping("/create")
    public String create(Model model){
        User user = (User) hostHolder.getUser().get("user");
        if (user==null||"admin".equals(user.getRole())){
            model.addAttribute("create",1);
        }else {
            model.addAttribute("create",0);
        }
        return "create";
    }

    @RequestMapping("/loginout")
    public String loginout(){
        String ticket = (String) hostHolder.getUser().get("ticket");
        userService.loginout(ticket);
        return "redirect:/in";
    }
}
