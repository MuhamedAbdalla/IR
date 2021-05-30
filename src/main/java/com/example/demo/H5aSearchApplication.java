package com.example.demo;

import com.example.demo.search.DatabaseQueryManager;
import com.example.demo.search.Helpers.Helpers;
import com.example.demo.search.PageParser;
import com.example.demo.search.SiteInfo;
import com.example.demo.search.indexing.Index;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@SpringBootApplication
@RestController
public class H5aSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(H5aSearchApplication.class, args);
	}

	@GetMapping
	public String search() {
		return view;
	}

	private static String view = "<!DOCTYPE html>\n" +
			"<html>\n" +
			"<head>\n" +
			"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" +
			"    <style>\n" +
			"* {box-sizing: border-box;}\n" +
			"\n" +
			"body {\n" +
			"  margin: 0;\n" +
			"  font-family: Arial, Helvetica, sans-serif;\n" +
			"}\n" +
			"\n" +
			".topnav {\n" +
			"  overflow: hidden;\n" +
			"  background-color: #e9e9e9;\n" +
			"}\n" +
			"\n" +
			".topnav a {\n" +
			"  float: left;\n" +
			"  display: block;\n" +
			"  color: black;\n" +
			"  text-align: center;\n" +
			"  padding: 14px 16px;\n" +
			"  text-decoration: none;\n" +
			"  font-size: 17px;\n" +
			"}\n" +
			"\n" +
			".topnav a:hover {\n" +
			"  background-color: #ddd;\n" +
			"  color: black;\n" +
			"}\n" +
			"\n" +
			".topnav a.active {\n" +
			"  background-color: #2196F3;\n" +
			"  color: white;\n" +
			"}\n" +
			"\n" +
			".topnav .search-container {\n" +
			"   margin: auto;\n" +
			"  width: 50%;\n" +
			"  border: 3px solid green;\n" +
			"  padding: 10px;\n" +
			"}\n" +
			"\n" +
			".topnav input[type=text] {\n" +
			"  padding: 6px;\n" +
			"  margin-top: 8px;\n" +
			"  font-size: 17px;\n" +
			"  border: none;\n" +
			"}\n" +
			"\n" +
			".topnav .search-container button {\n" +
			"  float: right;\n" +
			"  padding: 6px 10px;\n" +
			"  margin-top: 8px;\n" +
			"  margin-right: 16px;\n" +
			"  background: #ddd;\n" +
			"  font-size: 17px;\n" +
			"  border: none;\n" +
			"  cursor: pointer;\n" +
			"}\n" +
			"\n" +
			".topnav .search-container button:hover {\n" +
			"  background: #ccc;\n" +
			"}\n" +
			"\n" +
			"@media screen and (max-width: 600px) {\n" +
			"  .topnav .search-container {\n" +
			"    float: none;\n" +
			"  }\n" +
			"  .topnav a, .topnav input[type=text], .topnav .search-container button {\n" +
			"    float: none;\n" +
			"    display: block;\n" +
			"    text-align: left;\n" +
			"    width: 100%;\n" +
			"    margin: 0;\n" +
			"    padding: 14px;\n" +
			"  }\n" +
			"  .topnav input[type=text] {\n" +
			"    border: 1px solid #ccc;\n" +
			"  }\n" +
			"}\n" +
			"</style>\n" +
			"</head>\n" +
			"<body>\n" +
			"\n" +
			"<div class=\"topnav\">\n" +
			"    <div class=\"search-container\">\n" +
			"        <form action=\"/search\">\n" +
			"            <input type=\"text\" placeholder=\"Search...\" name=\"search\">\n" +
			"            <button type=\"submit\"><i class=\"fa fa-search\"></i></button>\n" +
			"        </form>\n" +
			"    </div>\n" +
			"</div>\n" +
			"\n" +
			"</body>\n" +
			"</html>\n";
}
