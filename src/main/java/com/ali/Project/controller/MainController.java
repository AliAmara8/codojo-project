package com.ali.Project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.ali.Project.models.LoginUser;
import com.ali.Project.models.Order;
import com.ali.Project.models.User;
import com.ali.Project.services.OrderService;
import com.ali.Project.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	private double total;
	
	 

	@GetMapping("/")
	public String index(Model model) {
	    model.addAttribute("newUser", new User());
	    model.addAttribute("newLogin", new LoginUser());
	    model.addAttribute("getStatesList", userService.getStatesList());
	    return "index.jsp";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("newUser") User newUser, 
			BindingResult result, Model model, HttpSession session) {

		
	
		User user = userService.register(newUser, result);
	     
	    if(result.hasErrors()) {
	        model.addAttribute("newLogin", new LoginUser());
	        return "index.jsp";
	    }
	    session.setAttribute("userId", user.getId());
	 
	    return "redirect:/dashboard";
	}

	@PostMapping("/login")
	public String login(@Valid @ModelAttribute("newLogin") LoginUser newLogin, 
			BindingResult result, Model model, HttpSession session) {
	     
		User user = userService.login(newLogin, result);
	 
	    if(result.hasErrors() || user==null) {
	        model.addAttribute("newUser", new User());
	        return "index.jsp";
	    }
	     
	    session.setAttribute("userId", user.getId());
	 
	    return "redirect:/dashboard";
	}
	
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
	 
		if(session.getAttribute("userId") == null) {
			return "redirect:/logout";
		}
		Long userId = (Long) session.getAttribute("userId");
		User user = userService.findById(userId);
		
		model.addAttribute("user", user);
	 
		return "home.jsp";
	}
	
	
	@GetMapping("/order/addOrder")
	public String addOrder(@ModelAttribute("newOrder") Order order, HttpSession session, Model model) {
		if(session.getAttribute("userId") == null) {
			return "redirect:/logout";
		}
		

		User user = userService.findById((Long)session.getAttribute("userId"));
    	model.addAttribute("user", user);
		
		
		 model.addAttribute("userId", user);
		 model.addAttribute("getMethodList", orderService.getMethodList());
		 model.addAttribute("getSizeList", orderService.getSizeList());
		 model.addAttribute("getCrustList", orderService.getCrustList());
		 model.addAttribute("getQuantityList", orderService.getQuantityList());
		 model.addAttribute("getToppingList", orderService.getToppingList());
		return "order.jsp";
	}
	
	@PostMapping("/order/addOrder")
	public String addNewProject(@ModelAttribute("newOrder") Order newOrder, HttpSession session) {
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/logout";
		}else {
		
		
		Order order=orderService.addOrder(newOrder);
			
			Long userId = (Long) session.getAttribute("userId");
			User user = userService.findById(userId);
			user.getOrders().add(order);
			userService.updateUser(user);
			return "redirect:/dashboard";
			}
		
	}
	
	@GetMapping("/order/{id}")
	public String viewProject(@PathVariable("id") Long id, HttpSession session, Model model) {
		
		if(session.getAttribute("userId") == null) {
			return "redirect:/logout";
		}
	
		Order order = orderService.findById(id);
		
		Map<String, Double> priceMethode= orderService.getMethodPrice();
		String ordM = order.getMethode();
		double prixM=priceMethode.get(ordM);
		
		Map<String, Double> priceSize= orderService.getSizePrice();
		String ordS = order.getSize();
		double prixS=priceSize.get(ordS);
		
		
		Map<String, Double> priceCrust= orderService.getCrustPrice();
		String ordC =order.getCrust();
		double prixC=priceCrust.get(ordC);
		
		Map<String, Integer> quantity= orderService.getQuantityList();
		String qty =order.getQuantity();
		int q=quantity.get(qty);
		  
		
		  
			/*
			 * Map<String, Double> priceTopping= orderService.getToppingPrice();
			 * List<String> ordT = order.getTopping(); double prixT=priceTopping.get(ordT);
			 */
		double deliveryFee = 6.0;
		 double price = (prixS+prixM+prixC)*q;
		 if(ordM.equals("Delivery")) {
			    total = price + deliveryFee;
			} else {
			    total = price;
			}
		  
		
		model.addAttribute("order", order);
		model.addAttribute("getSizePrice",prixS);
		
		model.addAttribute("getMethodPrice",prixM);
		model.addAttribute("getTotal",total);
		model.addAttribute("getPrice",price);
		model.addAttribute("getDeliveryFee",deliveryFee);
			/*
			 * model.addAttribute("getCrustPrice",prixC);
			 * model.addAttribute("getToppingPrice",prixT);
			 */
		 
		return "confirm.jsp";
	}
	
	 @GetMapping("/logout")
	    public String logout(HttpSession session) {
	    	session.invalidate();
	    	return "redirect:/";
	    }
	 
	 
	 @GetMapping("/ordres/{id}")
	 public String showtUser(@PathVariable("id") Long id,@ModelAttribute("Order") Order ord, Model model, HttpSession session) {
	 	
		 if(session.getAttribute("userId") == null) {
		 		return "redirect:/logout";
		 	}
		
		 User user = userService.findById((Long)session.getAttribute("userId"));
	    	
	 	List<Order> orders = orderService.getInOrders(user);
	 	model.addAttribute("orders", orders);
	 	model.addAttribute("user", user);
	 	model.addAttribute("ord", ord);

	 	return "vew.jsp";
	 }
	 
	 
	 
	 @GetMapping("/account/{id}")
	 public String showtUser(@PathVariable("id") Long id, HttpSession session, Model model) {
	 	
	 	if(session.getAttribute("userId") == null) {
	 		return "redirect:/logout";
	 	}
	 	
	 	
	 	
	 	User user = userService.findById(id);
	 	Order order = orderService.findById(id);	
	 	List<Order> orders = orderService.allOrders();
	 	List<Order> orderss = orderService.getInOrders(user);
	 	
	 	model.addAttribute("user", user);
	 	model.addAttribute("orders", orders);
	 	model.addAttribute("order", order);
	 	model.addAttribute("order", orderss);
	 	model.addAttribute("getStatesList", userService.getStatesList());
	 	return "account.jsp";
	 }
	 
	 @PostMapping("/account/{id}")
		public String editProject(
				@PathVariable("id") Long id, 
				@Valid @ModelAttribute("user") User user,Model model,
				BindingResult result, 
				HttpSession session) {
			
			if(session.getAttribute("userId") == null) {
				return "redirect:/logout";
			}

			if(result.hasErrors()) {
				return "account.jsp";
			}else {
				model.addAttribute("getStatesList", userService.getStatesList());
				User userr = userService.findById(id);
			   
				userService.updateUser(userr);
				return "redirect:/dashboard";
			}
		}

}
