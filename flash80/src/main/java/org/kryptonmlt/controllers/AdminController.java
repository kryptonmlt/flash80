package org.kryptonmlt.controllers;

import lombok.extern.slf4j.Slf4j;
import org.kryptonmlt.services.MemoryCache;
import org.kryptonmlt.utils.FlashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
public class AdminController {

    @Autowired
    private MemoryCache memoryCache;

    @GetMapping("/flash80/cache")
    String admin(Model model) {
        model.addAttribute("cacheObjects", FlashUtils.toUICacheObject(memoryCache.getCache()));
        return "admin";
    }

    @RequestMapping(value = "/flash80/cache/delete", method = RequestMethod.POST)
    @ResponseBody
    String delete(@RequestBody String key) {
        memoryCache.getCache().remove(key);
        return "OK";
    }
}
