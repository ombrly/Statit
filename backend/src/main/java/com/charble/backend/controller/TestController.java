package com.charble.backend.controller;

import com.charble.backend.service.provider.DataCommonsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController
{
    private final DataCommonsService dataCommonsService;

    public TestController(DataCommonsService dataCommonsService)
    {
        this.dataCommonsService = dataCommonsService;
    }

    @GetMapping("/update-baselines")
    public String triggerDataCommonsUpdate()
    {
        System.out.println("Starting Data Commons fetch...");

        dataCommonsService.fetchAndSaveGlobalBaselines();

        return "Data Commons update triggered";
    }

}
