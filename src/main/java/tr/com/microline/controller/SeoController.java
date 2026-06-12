package tr.com.microline.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tr.com.microline.service.SitemapService;

/** Dil öneksiz SEO uçları. */
@Controller
public class SeoController {

    private final SitemapService sitemapService;

    public SeoController(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        return sitemapService.buildXml();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return """
                User-agent: *
                Allow: /
                Disallow: /admin/
                Disallow: /tr/sepet
                Disallow: /en/cart
                Disallow: /tr/siparis-talebi
                Disallow: /en/order-request

                Sitemap: %s/sitemap.xml
                """.formatted(sitemapService.baseUrl());
    }
}
