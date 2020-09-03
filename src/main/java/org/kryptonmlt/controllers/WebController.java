package org.kryptonmlt.controllers;

import org.kryptonmlt.config.ApplicationProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
public class WebController {


    @Autowired
    private ApplicationProps applicationProps;

    public HashMap<String, ApplicationProps.Server> sites = new HashMap<>();

    @PostConstruct
    private void init() {
        for (ApplicationProps.Server host : applicationProps.getHosts()) {
            for (String site : host.getSites()) {
                sites.put(site, host);
            }
        }
    }

    @RequestMapping(value = "**", method = RequestMethod.GET)
    public String get(HttpServletRequest request) {
        ApplicationProps.Server server = sites.get(request.getRemoteHost());
        if(server != null){

        }
        return "redirect:/404.html";
    }
}
