package pl.radekbonk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ProductController {

	@GetMapping(value = "/products")
	public String getProducts() {

		return "page_product";
	}

}
