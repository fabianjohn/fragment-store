package com.fabcode.storefragment.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fabcode.storefragment.dao.OrderDAO;
import com.fabcode.storefragment.dao.ProductDAO;
import com.fabcode.storefragment.entity.Product;
import com.fabcode.storefragment.form.CustomerForm;
import com.fabcode.storefragment.model.CartInfo;
import com.fabcode.storefragment.model.CustomerInfo;
import com.fabcode.storefragment.model.ProductInfo;
import com.fabcode.storefragment.pagination.PaginationResult;
import com.fabcode.storefragment.utils.Utils;
import com.fabcode.storefragment.validator.CustomerFormValidator;

import jakarta.servlet.http.HttpServletRequest;




//import entity.Product;



@Controller
@Transactional
public class MainController {
	
	
	@Autowired
	 private OrderDAO orderDAO;

	   @Autowired
	   private ProductDAO productDAO;
	   

	   @Autowired
	   private CustomerFormValidator customerFormValidator;

	   @InitBinder
	   public void myInitBinder(WebDataBinder dataBinder) {
	      Object target = dataBinder.getTarget();
	      if (target == null) {
	         return;
	      }
	      System.out.println("Target=" + target);

	      // Case update quantity in cart
	      // (@ModelAttribute("cartForm") @Validated CartInfo cartForm)
	      if (target.getClass() == CartInfo.class) {

	      }

	      // Case save customer information.
	      // (@ModelAttribute @Validated CustomerInfo customerForm)
	      else if (target.getClass() == CustomerForm.class) {
	         dataBinder.setValidator(customerFormValidator);
	      }

	   }
	   
	   
	   @RequestMapping("/403")
	   public String accessDenied() {
	      return "/403";
	   }

	   @RequestMapping("/test")
	   public String New() {
	      return "hom";
	   }
	   
	   // Product List
	   @RequestMapping({ "/productList" })
	   public String listProductHandler(Model model, //
	         @RequestParam(value = "name", defaultValue = "") String likeName,
	         @RequestParam(value = "page", defaultValue = "1") int page) {
	      final int maxResult = 50;
	      final int maxNavigationPage = 15;

	      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
	            maxResult, maxNavigationPage, likeName);

	      model.addAttribute("paginationProducts", result);
	      return "productList";
	   }

	   @RequestMapping({ "/buyProduct" })
	   public String listProductHandler(HttpServletRequest request, Model model, //
	         @RequestParam(value = "code", defaultValue = "") String code) {

	      Product product = null;
	      if (code != null && code.length() > 0) {
	         product = productDAO.findProduct(code);
	      }
	      if (product != null) {

	         //
	         CartInfo cartInfo = Utils.getCartInSession(request);

	         ProductInfo productInfo = new ProductInfo(product);

	         cartInfo.addProduct(productInfo, 1);
	      }

	      return "redirect:/shoppingCart";
	   }

	   @RequestMapping({ "/shoppingCartRemoveProduct" })
	   public String removeProductHandler(HttpServletRequest request, Model model, //
	         @RequestParam(value = "code", defaultValue = "") String code) {
	      Product product = null;
	      if (code != null && code.length() > 0) {
	         product = productDAO.findProduct(code);
	      }
	      if (product != null) {

	         CartInfo cartInfo = Utils.getCartInSession(request);

	         ProductInfo productInfo = new ProductInfo(product);

	         cartInfo.removeProduct(productInfo);

	      }

	      return "redirect:/shoppingCart";
	   }

	   // POST: Update quantity for product in cart
	   @RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.POST)
	   public String shoppingCartUpdateQty(HttpServletRequest request, //
	         Model model, //
	         @ModelAttribute("cartForm") CartInfo cartForm) {

	      CartInfo cartInfo = Utils.getCartInSession(request);
	      cartInfo.updateQuantity(cartForm);

	      return "redirect:/shoppingCart";
	   }

	   // GET: Show cart.
	   @RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.GET)
	   public String shoppingCartHandler(HttpServletRequest request, Model model) {
	      CartInfo myCart = Utils.getCartInSession(request);

	      model.addAttribute("cartForm", myCart);
	      return "shoppingCart";
	   }

	   // GET: Enter customer information.
	   @RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.GET)
	   public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {

	      CartInfo cartInfo = Utils.getCartInSession(request);

	      if (cartInfo.isEmpty()) {

	         return "redirect:/shoppingCart";
	      }
	      CustomerInfo customerInfo = cartInfo.getCustomerInfo();

	      CustomerForm customerForm = new CustomerForm(customerInfo);

	      model.addAttribute("customerForm", customerForm);

	      return "shoppingCartCustomer";
	   }

	   // POST: Save customer information.
	   @RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.POST)
	   public String shoppingCartCustomerSave(HttpServletRequest request, //
	         Model model, //
	         @ModelAttribute("customerForm") @Validated CustomerForm customerForm, //
	         BindingResult result, //
	         final RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, MessagingException {

	      if (result.hasErrors()) {
	         customerForm.setValid(false);
	         // Forward to reenter customer info.
	         return "shoppingCartCustomer";
	      }

	      customerForm.setValid(true);
	      CartInfo cartInfo = Utils.getCartInSession(request);
	      CustomerInfo customerInfo = new CustomerInfo(customerForm);
	      cartInfo.setCustomerInfo(customerInfo);
	     String siteURL = Utils.getSiteURL(request);
	      
	     
	      orderDAO.sendVerificationEmail(cartInfo, siteURL);
			model.addAttribute("pageTitle", "Registration successfull");
			System.out.println("Mail sent to " + cartInfo.getCustomerInfo().getEmail());

	      return "redirect:/shoppingCartConfirmation";
	   }
       
	   
	  

	// GET: Show information to confirm.
	   @RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.GET)
	   public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
	      CartInfo cartInfo = Utils.getCartInSession(request);

	      if (cartInfo == null || cartInfo.isEmpty()) {

	         return "redirect:/shoppingCart";
	      } else if (!cartInfo.isValidCustomer()) {

	         return "redirect:/shoppingCartCustomer";
	      }
	      model.addAttribute("myCart", cartInfo);

	      return "shoppingCartConfirmation";
	   }

	   // POST: Submit Cart (Save)
	   @RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.POST)
	   public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
	      CartInfo cartInfo = Utils.getCartInSession(request);

	      if (cartInfo.isEmpty()) {
	    	  
	    	  
	         return "redirect:/shoppingCart";
	      } else if (!cartInfo.isValidCustomer()) {
	    	  

	         return "redirect:/shoppingCartCustomer";
	      }
	      try {
	         orderDAO.saveOrder(cartInfo);
	         
	         
	      } catch (Exception e) {

	         return "shoppingCartConfirmation";
	      }

	      // Remove Cart from Session.
	      Utils.removeCartInSession(request);

	      // Store last cart.
	      Utils.storeLastOrderedCartInSession(request, cartInfo);

	      return "redirect:/shoppingCartFinalize";
	   }

	   @RequestMapping(value = { "/shoppingCartFinalize" }, method = RequestMethod.GET)
	   public String shoppingCartFinalize(HttpServletRequest request, Model model) {
		   
	      CartInfo lastOrderedCart = Utils.getLastOrderedCartInSession(request);

	      if (lastOrderedCart == null) {
	         return "redirect:/shoppingCart";
	      }
	      
	      
	      model.addAttribute("lastOrderedCart", lastOrderedCart);
	      return "shoppingCartFinalize";
	   }
	   
	   

	   @RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
	   public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
	         @RequestParam("code") String code) throws IOException {
	      Product product = null;
	      if (code != null) {
	         product = this.productDAO.findProduct(code);
	      }
	      if (product != null && product.getImage() != null) {
	         response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
	         response.getOutputStream().write(product.getImage());
	      }
	      response.getOutputStream().close();
	   }
	   

/* My ideal work*/
	// Product List
	   @RequestMapping("/listpro ")
		public String viewindexPage(Model model) {
			List<Product> listproduct = productDAO.findAll(); 
			model.addAttribute("listproduct", listproduct);
			System.out.print("Get /listproduct ");
		
			return "index";
		}
	   
	   
	
	  //search product by keyword
	   @GetMapping("/home")
	    public String viewHomePage(Model model, Product product, @RequestParam("keyword") String keyword) {
		   if(keyword != null) {
	        List<Product>list =  productDAO.findByKeyword(keyword) ; //listAll(keyword);
	        model.addAttribute("list", list );
	        model.addAttribute("keyword", keyword);
		   }else {
			   List<Product>list = productDAO.findAll();
			   model.addAttribute("list", list);
		       // System.out.println("list" + list);
		   }
	        
	         
	        return "home";
	    }
	   
	  
	   
	   @GetMapping("/####")
	   public String passParametersWithModelMap(@RequestParam("name") String name,  Product product,
			   @RequestParam("price") Double price, @RequestParam(value = "code", defaultValue = "") String code, ModelMap map) {
		   
		   
		      if (code != null && code.length() > 0) {
		    
		   
		   map.put("code", code);
		   map.put("name", name);
		   map.put("price", price);
	       map.addAttribute("welcomeMessage", "welcome");
	       map.addAttribute("message", "Baeldung");
		      }
	       return "model_view";
	   }
	   
		      
		      
	   @GetMapping("/showViewPage")
	   public String passParametersWithModel(Model model) {
	       Map<String, String> map = new HashMap<>();
	       map.put("spring", "mvc");
	       model.addAttribute("message", "Baeldung");
	       model.mergeAttributes(map);
	       return "view/viewPage";
	   }
	   
	   //show product details
	   @GetMapping(value = "/productDetail")
	   public ModelAndView passParametersWithModelAndView(Product product, @RequestParam("code") String code, Model model) {
		   if(code !=null) {
			   List<Product> listproduct = productDAO.findAll(); 
				model.addAttribute("listproduct", listproduct);
			   product = productDAO.findProduct(code);
		   }
	       ModelAndView modelAndView = new ModelAndView();
	       modelAndView.addObject("obj", product);
	       modelAndView.setViewName("model_view");
	       return modelAndView;
	   }
	  
	    
	  
}

