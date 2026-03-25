package com.mtp.config.center.controller;

import com.mtp.config.center.entity.ApplicationRegistryEntity;
import com.mtp.config.center.model.R;
import com.mtp.config.center.service.ApplicationRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application-registry")
public class ApplicationRegistryController {

    @Autowired
    private ApplicationRegistryService applicationRegistryService;

    @GetMapping("/list")
    public R list(@RequestParam(defaultValue = "1") int page,
                  @RequestParam(defaultValue = "10") int size,
                  @RequestParam(required = false) String applicationName) {
        return applicationRegistryService.list(page, size, applicationName);
    }

    @PostMapping("/add")
    public R add(@RequestBody ApplicationRegistryEntity entity) {
        return applicationRegistryService.add(entity);
    }

    @PutMapping("/update")
    public R update(@RequestBody ApplicationRegistryEntity entity) {
        return applicationRegistryService.update(entity);
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        return applicationRegistryService.delete(id);
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable Long id) {
        return applicationRegistryService.getById(id);
    }
}