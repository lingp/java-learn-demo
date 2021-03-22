package com.lin.demo.action;

import com.lin.demo.service.IQueryService;
import com.lin.framework.annotation.Autowired;
import com.lin.framework.annotation.Controller;
import com.lin.framework.annotation.Params;
import com.lin.framework.annotation.RequestMapping;
import com.lin.framework.webmvc.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class PageAction {

    @Autowired
    IQueryService queryService;

    @RequestMapping("/first.html")
    public ModelAndView query(@Params("teacher") String teacher) {
        String result = queryService.query(teacher);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("teacher", teacher);

        model.put("data", result);
        model.put("token", "123456");
        return new ModelAndView("first.html",model);
    }


}
