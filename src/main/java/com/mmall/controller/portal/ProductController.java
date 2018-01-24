package com.mmall.controller.portal;

import com.mmall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by xxtang on 2018/1/23.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private ProductService productService;
}
