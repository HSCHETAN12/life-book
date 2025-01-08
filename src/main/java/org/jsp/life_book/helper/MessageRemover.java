package org.jsp.life_book.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class MessageRemover {
		
	
	public void remove()
	{
		ServletRequestAttributes attributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		HttpSession httpSession=attributes.getRequest().getSession();
		httpSession.removeAttribute("pass");
		httpSession.removeAttribute("fail");
	}
}
