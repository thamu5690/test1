package org.vtop.CourseRegistration.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.vtop.CourseRegistration.NetAssist;
import org.vtop.CourseRegistration.model.EmployeeProfile;
import org.vtop.CourseRegistration.model.PatternTimeMasterModel;
import org.vtop.CourseRegistration.model.SemesterDetailsModel;
import org.vtop.CourseRegistration.model.SlotTimeMasterModel;
import org.vtop.CourseRegistration.model.StudentsLoginDetailsModel;
import org.vtop.CourseRegistration.repository.RegistrationLogRepository;
import org.vtop.CourseRegistration.repository.WishlistRegistrationRepository;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationService;
import org.vtop.CourseRegistration.service.EmployeeProfileService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumCreditService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumDetailService;
import org.vtop.CourseRegistration.service.RegistrationLogService;
import org.vtop.CourseRegistration.service.SemesterDetailsService;
import org.vtop.CourseRegistration.service.StudentLoginDetailsService;
import org.vtop.CourseRegistration.service.TimeTablePatternDetailService;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;

@Controller
public class CourseRegistrationPageController 
{
	@Autowired private CourseRegistrationService courseRegistrationService;

	@Autowired private RegistrationLogService registrationLogService;	
	
	@Autowired private EmployeeProfileService employeeProfileService; 
	
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	
	@Autowired private TimeTablePatternDetailService timeTablePatternDetailService;
	
	@Autowired private SemesterDetailsService semesterDetailsService;
	
	@Autowired private StudentLoginDetailsService studentLoginDetailsService;
	
	@Autowired private RegistrationLogRepository registrationLogRepository;
	
	@Autowired private WishlistRegistrationRepository wishlistRegistrationRepository;
	
	@Autowired private ProgrammeSpecializationCurriculumCreditService programmeSpecializationCurriculumCreditService;
	
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
		

	private static final Logger logger = LogManager.getLogger(CourseRegistrationPageController.class);
	private static final String RegErrorMethod = "SSS4EM2021WL";
	

	@RequestMapping(value = "SessionTimedOut", method = { RequestMethod.POST, RequestMethod.GET })
	public String sessionError(@CookieValue(value = "RegisterNumber") String registerNumber, Model model, 
						HttpServletRequest request, HttpServletResponse response, HttpSession session) 
						throws ServletException, IOException 
	{
		String page = "";		
		Cookie[] cookies = request.getCookies();		
		
		if (cookies!=null)
		{
			for (Cookie cookie : cookies) 
			{
				if(cookie.getName().equals(registerNumber))
				{
					if (registrationLogService.isExist(registerNumber)) 
					{
						registrationLogService.UpdateLogoutTimeStamp(request.getRemoteAddr(), registerNumber);
						
						cookie = new Cookie("RegisterNumber", null);
						cookie.setMaxAge(0);
						cookie.setSecure(true);
						cookie.setHttpOnly(true);
						response.addCookie(cookie);
						request.getSession().invalidate();				
					}
				}				
				
				model.addAttribute("message", "Session Expired");
				model.addAttribute("error", "Try Logout and Log-in");
				model.addAttribute("errno", 3);
				page = "CustomErrorPage";
			}			
		}
		else
		{
			courseRegCommonFn.callCaptcha(request,response,session,model);			
			model.addAttribute("flag", 2);			
			page = "redirectpage";							
		}	
		
		return page;
	}

	@RequestMapping(value= "/", method = {RequestMethod.GET, RequestMethod.POST})
	public String home(HttpServletRequest httpServletRequest, Model model, HttpSession session, 
							HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String userAgent = httpServletRequest.getHeader("user-agent");
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		Version browserVersion = ua.getBrowserVersion();
		String browserName = ua.getBrowser().toString();
		String userSessionId = null;
		
		String currentDateTimeStr;	
		
		Date currentDateTime = new Date();
		
		currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
		
		model.addAttribute("CurrentDateTime", currentDateTimeStr);
		
		session.setAttribute("baseURL", NetAssist.getBaseURL(httpServletRequest));		

		int majVersion = Integer.parseInt(browserVersion.getMajorVersion());
		
		if (browserName.equalsIgnoreCase("Firefox") && majVersion < 50) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Browser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
		} 
		else if (browserName.equalsIgnoreCase("Chrome") && majVersion < 50) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Browser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		}
		else if (browserName.equalsIgnoreCase("EDGE14") && majVersion == 14) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		} 
		else if (browserName.equalsIgnoreCase("OPERA") && majVersion < 40) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		} 
		else if (browserName.contains("IE")) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		}

		userSessionId = (String) session.getAttribute("userSessionId");

		if (userSessionId == null) 
		{
			session.setAttribute("userSessionId", session.getId());
		}
		
		return "InstructionPage";
	}

	@PostMapping("viewStudentLogin")
	public String viewStudentLogin(Model model, HttpServletRequest request, HttpSession session, 
						HttpServletResponse response) throws ServletException, IOException 
	{
		String currentDateTimeStr="",userSessionId="";
		session.setAttribute("baseURL", NetAssist.getBaseURL(request));
		userSessionId = (String) session.getAttribute("userSessionId");
		
		if (userSessionId == null) 
		{
			session.setAttribute("userSessionId", session.getId());
		}
		
		currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());
		
		model.addAttribute("CurrentDateTime", currentDateTimeStr);
		session.setAttribute("CAPTCHA",session.getAttribute("CAPTCHA"));
		return "StudentLogin";
	}
	
	@PostMapping("viewStudentLogin1")
	public String viewStudentLogin1(Model model, HttpServletRequest request, HttpSession session, 
						HttpServletResponse response) throws ServletException, IOException 
	{
		session.setAttribute("baseURL", NetAssist.getBaseURL(request));
		String userSessionId = (String) session.getAttribute("userSessionId");

		if (userSessionId == null) 
		{
			session.setAttribute("userSessionId", session.getId());
		}
		
		courseRegCommonFn.callCaptcha(request, response, session, model);
		session.setAttribute("CAPTCHA", session.getAttribute("CAPTCHA"));
		
		return "StudentLogin::test";
	}	

	@RequestMapping(value = "ServerLimit", method = { RequestMethod.POST, RequestMethod.GET })
	public String serverLimit(Model model, HttpSession session, HttpServletRequest request) throws ServletException 
	{
		String page = "CustomErrorPage";
		String baseURL = NetAssist.getBaseURL(request);
		logger.trace("BaseUrl - " + baseURL);

		 

		request.getSession().invalidate();
		model.addAttribute("message", "");
		model.addAttribute("error", " Please Note: Try one of the following Servers <br/><br/>");
		model.addAttribute("errno", 99);

		return page;
	}

	@RequestMapping(value = "AlreadyLogin", method = { RequestMethod.POST, RequestMethod.GET })
	public String AlreadyLogin(Model model,HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) throws ServletException 
	{
		String page = "CustomErrorPage";

		/*Testing
		Cookie cookie = new Cookie("RegisterNumber", null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		request.getSession().invalidate();
		model.addAttribute("message", " V TOP ");
		model.addAttribute("error", " Thank you For Using V TOP Course Registration Portal .");*/
		
		/**/
		model.addAttribute("message", "Multi-Tab Access");
		model.addAttribute("error", "Multiple Tabs Access prevented !!!");
		model.addAttribute("errno", 6);
		return page;
	}
	
	@GetMapping("signOut")
	public String signOut(Model model,HttpServletRequest request) 
	{
		model.addAttribute("message", " V TOP ");
		model.addAttribute("error", " Thank you For Using V TOP Course Registration Portal .");
		request.getSession().invalidate();	
		return "CustomErrorPage";
	}
	
	@GetMapping("noscript")
	public String noscript(Model model) 
	{
		model.addAttribute("message", "JavaScript Error");
		model.addAttribute("error", "Kindly Enable JavaScript in Your Browser to Access V-TOP.");
		return "ErrorPage";
	}

	@RequestMapping(value = "processLogout", method = { RequestMethod.POST, RequestMethod.GET })
	public String doLogout(HttpSession session, HttpServletRequest request, HttpServletResponse response, 
						Model model) throws ServletException, IOException 
	{
		String page = "", info = null, pageAuthKey = "";
		String currentDateTimeStr = "",logoutMsg="";
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		int loAllowFlag = 2, pageAuthStatus = 2;
		int regCredit=0, wlRegCredit=0;
		Integer minCredit = (Integer) session.getAttribute("minCredit");
		Integer maxCredit = (Integer) session.getAttribute("maxCredit");
		String IpAddress=(String) session.getAttribute("IpAddress");
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try 
		{
			if ((registerNumber!=null) && (registrationLogService.isExist(registerNumber) && (pageAuthStatus == 1)))
			{
				info = (String) session.getAttribute("info");
				
				loAllowFlag = 1;
				
				if (loAllowFlag == 1) 
				{
					registrationLogService.UpdateLogoutTimeStamp(IpAddress,registerNumber);
					model.addAttribute("flag", 4);			
					page = "redirectpage";
				}
				else
				{
					currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());
					logoutMsg="Minimum of "+minCredit+" credits needed for 'Sign Out'.";
					model.addAttribute("CurrentDateTime", currentDateTimeStr);
					model.addAttribute("regCredit", regCredit);
					model.addAttribute("wlCredit", wlRegCredit);
					model.addAttribute("maxCredit", maxCredit);
					model.addAttribute("studentDetails", session.getAttribute("studentDetails"));
					model.addAttribute("logoutMsg", logoutMsg);
					
					return "mainpages/MainPage";
				}
			}
			else
			{
				model.addAttribute("flag", 4);			
				page = "redirectpage";
			}
			
			model.addAttribute("info", info);
			Cookie cookie = new Cookie("RegisterNumber", null);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			request.getSession().invalidate();
			return page;
		} 
		catch (Exception ex) 
		{
			model.addAttribute("info", "Login with your Username and Password");
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"processLogout", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			courseRegCommonFn.callCaptcha(request,response,session,model);
			session.setAttribute("CAPTCHA",session.getAttribute("CAPTCHA"));
			page = "StudentLogin";
			return page;
		}	
	}
	

	@PostMapping(value = "ViewCredits")
	public String ViewCredits(Model model, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		String urlPage = "";
		Integer regCredit = 0, regCount = 0;
		String IpAddress=(String) session.getAttribute("IpAddress");
		Integer maxCredit = (Integer) session.getAttribute("maxCredit");
		try
		{
			if (registerNumber!=null)
			{				
				regCount = wishlistRegistrationRepository.findRegisterNumberTCCount(semesterSubId,classGroupId, registerNumber);
				regCredit = wishlistRegistrationRepository.findRegisterNumberTotalCredits(semesterSubId,classGroupId,registerNumber);
				
				model.addAttribute("regCredit", regCredit);
				model.addAttribute("regCount", regCount);
				model.addAttribute("maxCredit", maxCredit);
				model.addAttribute("WaitingListStatus", WaitingListStatus);
				
				urlPage = "mainpages/MainPage::creditsFragment";
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"ViewCredits", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}		

		return urlPage;
	}
	
	@PostMapping("viewCurriculumCredits")
	public String viewCurriculumCredits(HttpSession session,Model model)
	{
		String urlPage="";
		List<Object[]> cclCtgCreditList = new ArrayList<Object[]>();
		List<String> regnoList = new ArrayList<String>();
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
		int studyStartYear = (int) session.getAttribute("StudyStartYear");
		String studentStudySystem = (String) session.getAttribute("studentStudySystem");
		String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
		Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String registerNo = (String) session.getAttribute("RegisterNumber");
		String OldRegNo = (String) session.getAttribute("OldRegNo");
		String IpAddress=(String) session.getAttribute("IpAddress");
		
		try 
		{
			if(registerNo!=null)
			{
				regnoList.add(registerNo);
				if ((OldRegNo != null) && (!OldRegNo.equals("")))
				{
					regnoList.add(OldRegNo);
				}
				cclCtgCreditList = programmeSpecializationCurriculumCreditService.getCurrentSemRegCurCtgCreditByRegisterNo(
			                   			programSpecId, studyStartYear, curriculumVersion, semesterSubId, regnoList, classGroupId);
				
				model.addAttribute("cclCtgCreditList", cclCtgCreditList);
				model.addAttribute("studentStudySystem", studentStudySystem);
				model.addAttribute("WaitingListStatus", WaitingListStatus);
				urlPage = "mainpages/ViewCurriculumCredits::section";
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		} 
		catch (Exception e) 
		{
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"viewCurriculumCredits", registerNo, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNo);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}
		return urlPage;
	}
	
	@PostMapping("viewRegistered")
	public String viewRegistered(Model model, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer updateStatus = 1;
		int allowStatus = 2;		
		String urlPage = "";
		String msg = null, infoMsg = "";
		
		try
		{
			if (registerNumber!=null)
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");												
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				SemesterDetailsModel sdm = new SemesterDetailsModel();	
				
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				StudentsLoginDetailsModel studentsLoginDetailsModel = new StudentsLoginDetailsModel();
				studentsLoginDetailsModel = studentLoginDetailsService.getOne(registerNumber);
								
				switch(allowStatus)
				{
					case 1:
						
						List<Object[]> courseRegistrationModel = wishlistRegistrationRepository.findByRegisterNumberAsObject(
										semesterSubId, classGroupId, registerNumber);;
						sdm = semesterDetailsService.getOne(semesterSubId);
						
						
						model.addAttribute("cDate", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new Date()));
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("sdm", sdm);
						model.addAttribute("studentsLoginDetailsModel", studentsLoginDetailsModel);
						model.addAttribute("showFlag", 0);
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
																		
						session.removeAttribute("registrationOption");
						urlPage = "mainpages/ViewRegistered::section";
						break;				
					
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}			
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}			
		}
		catch(Exception e)
		{
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"viewRegistered", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;
		}			
		return urlPage;
	}
	
	@PostMapping(value = "getSchoolWiseGuideList")
	public String getSchoolWiseGuideList(String guideSchoolOpt , Model model, HttpSession session, HttpServletRequest request) 
	{	
		
		String urlPage = "";
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress=(String) session.getAttribute("IpAddress");
		
		try
		{
			Integer costCentreId = 0;
			if ((guideSchoolOpt != null) && (!guideSchoolOpt.equals("")))
			{
				costCentreId =Integer.parseInt(guideSchoolOpt);
			}
			List<EmployeeProfile> employeeList = employeeProfileService.getByCentreId(costCentreId);
			model.addAttribute("employeeList", employeeList);
			model.addAttribute("costCentreId", costCentreId);
			urlPage = "mainpages/ProjectRegistration::ProjectGuideFragment";			
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString()+" <-Guide School-> "+guideSchoolOpt, RegErrorMethod+"CourseRegistrationPageController", 
					"getSchoolWiseGuideList", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		
		return urlPage;	
	}
	
	List<String> getStartingTimeTableSlots(Integer patternId, List<PatternTimeMasterModel> list1)
	{		
		BigDecimal bg;
		List<Object[]> listMax = timeTablePatternDetailService.getMaxSlots(patternId);
		List<String> listTimeTableSlots = new ArrayList<String>();
		
		int fnMax = 0, anMax = 0, enMax=0;
		String sesMax = "";
		try
		{
			for (int m= 0; m< listMax.size(); m++)
			{
				sesMax =listMax.get(m)[1].toString(); 
				if (sesMax.equals("FN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					fnMax = bg.intValue();
				}
				if (sesMax.equals("AN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					anMax =bg.intValue();
				}
				if (sesMax.equals("EN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					enMax = bg.intValue();
				}			
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
							
		//          THEORY STARTING TIMINGS 
		int i = 1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("FN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}				
		}
		i = i-1;
		if (i < fnMax)
		{
			for (int j=1; j <= fnMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		listTimeTableSlots.add("Lunch");
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("AN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}
		}
		i = i-1;
		if (i < anMax)
		{
			for (int j=1; j <= anMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("EN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}				
		}	
		i = i-1;
		if (i < enMax)
		{
			for (int j=1; j <= enMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
							
		for (int k = 0; k < listTimeTableSlots.size(); k++)
		{
		
		}
		
		return listTimeTableSlots;
	}
	
	
	List<String> getEndingTimeTableSlots(Integer patternId, List<PatternTimeMasterModel> list1)
	{		
		BigDecimal bg;
		List<Object[]> listMax = timeTablePatternDetailService.getMaxSlots(patternId);
		List<String> listTimeTableSlots = new ArrayList<String>();
		
		int fnMax = 0, anMax = 0, enMax=0;
		String sesMax;
		try
		{
			for (int m= 0; m< listMax.size(); m++)
			{
				sesMax =listMax.get(m)[1].toString(); 
				if (sesMax.equals("FN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					fnMax = bg.intValue();
				}
				if (sesMax.equals("AN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					anMax =bg.intValue();
				}
				if (sesMax.equals("EN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					enMax = bg.intValue();
				}			
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		
		//          THEORY STARTING TIMINGS 
		int i = 1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("FN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}				
		}
		i = i-1;
		if (i < fnMax)
		{
			for (int j=1; j <= fnMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		listTimeTableSlots.add("Lunch");
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("AN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}
		}
		i = i-1;
		if (i < anMax)
		{
			for (int j=1; j <= anMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("EN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}				
		}	
		i = i-1;
		if (i < enMax)
		{
			for (int j=1; j <= enMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
							
		return listTimeTableSlots;
	}
	
	List<Object[]> getTimeTableSlots(String semesterSubId, String registerNumber, Integer patternId,
			List<SlotTimeMasterModel> slotTimeMasterList) 
	{
		
	BigDecimal bg;
	List<Object[]> listMax = timeTablePatternDetailService.getMaxSlots(patternId);
	List<String> listTimeTableSlots = new ArrayList<String>();
	List<Object[]> listTimeTableSlots1 = new ArrayList<Object[]>();

	int fnMax = 0, anMax = 0, enMax = 0;
	String sesMax;
	try 
	{
		for (int m = 0; m < listMax.size(); m++) 
		{
			sesMax = listMax.get(m)[1].toString();
			if (sesMax.equals("FN")) 
			{
				bg = new BigDecimal(listMax.get(m)[0].toString());
				fnMax = bg.intValue();
			}
			if (sesMax.equals("AN")) 
			{
				bg = new BigDecimal(listMax.get(m)[0].toString());
				anMax = bg.intValue();
			}
			if (sesMax.equals("EN")) 
			{
				bg = new BigDecimal(listMax.get(m)[0].toString());
				enMax = bg.intValue();
			}
		}
	} 
	catch (Exception ex) 
	{
		ex.printStackTrace();
	}

	int i = 1;
	
	List<Object[]> regSlots = courseRegistrationService.getCourseRegWlSlotByStudent(semesterSubId,
					registerNumber, patternId);
	Map<String, List<Object[]>> tempMap = new HashMap<>();

	for (Object[] parameters : regSlots) 
	{
		if (tempMap.containsKey(parameters[1])) 
		{
			List<Object[]> temp = tempMap.get(parameters[1]);
			temp.add(parameters);
			tempMap.put(parameters[1].toString(), temp);
		} 
		else 
		{
			List<Object[]> temp = new ArrayList<>();
			temp.add(parameters);
			tempMap.put(parameters[1].toString(), temp);
		}
	}

	for (SlotTimeMasterModel ls : slotTimeMasterList) 
	{
		String slName = ls.getSession();
		if (slName.equals("FN")) 
		{
			String[] tempArr = new String[2];
			tempArr[0] = ls.getStmPkId().getSlot();
			if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
			{
				for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
				{
					if (obj[0].equals(ls.getStmPkId().getSlot())) 
					{
						tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
						tempArr[1] = "#CCFF33";
						break;
					} 
					else 
					{
						tempArr[1] = "";
					}
				}
			} 
			else 
			{
				tempArr[1] = "";
			}

			listTimeTableSlots1.add(tempArr);
			i++;
		}
	}
	
	i = i - 1;
	if (i < fnMax) 
	{
		for (int j = 1; j <= fnMax - i; j++) 
		{
			listTimeTableSlots.add("-");
			String[] tempArr = new String[2];
			tempArr[0] = "-";
			tempArr[1] = "";
			listTimeTableSlots1.add(tempArr);
		}
	}

	String[] tempArrLunch = new String[2];
	tempArrLunch[0] = "Lunch";
	tempArrLunch[1] = "#e2e2e2";
	listTimeTableSlots1.add(tempArrLunch);
	i = 1;
	for (SlotTimeMasterModel ls : slotTimeMasterList) 
	{
		String slName = ls.getSession();
		if (slName.equals("AN")) {
			String[] tempArr = new String[2];
			tempArr[0] = ls.getStmPkId().getSlot();
			if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
			{
				for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
				{
					if (obj[0].equals(ls.getStmPkId().getSlot())) 
					{
						tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
						tempArr[1] = "#CCFF33";
						break;
					} 
					else 
					{
						tempArr[1] = "";
					}
				}

			} 
			else 
			{
				tempArr[1] = "";
			}

			listTimeTableSlots1.add(tempArr);
			i++;
		}
	}
	i = i - 1;
	if (i < anMax) 
	{
		for (int j = 1; j <= anMax - i; j++) 
		{
			listTimeTableSlots.add("-");
			String[] tempArr = new String[2];
			tempArr[0] = "-";
			tempArr[1] = "";
			listTimeTableSlots1.add(tempArr);
		}
	}

	i = 1;
	for (SlotTimeMasterModel ls : slotTimeMasterList) 
	{
		String slName = ls.getSession();
		if (slName.equals("EN")) 
		{
			String[] tempArr = new String[2];
			tempArr[0] = ls.getStmPkId().getSlot();
			if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
			{
				for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
				{
					if (obj[0].equals(ls.getStmPkId().getSlot())) 
					{
						tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
						tempArr[1] = "#CCFF33";
						break;
					} 
					else 
					{
						tempArr[1] = "";
					}
				}
			} 
			else 
			{
				tempArr[1] = "";
			}

			listTimeTableSlots1.add(tempArr);
			i++;
		}
	}
	i = i - 1;
	if (i < enMax) 
	{
		for (int j = 1; j <= enMax - i; j++) 
		{
			listTimeTableSlots.add("-");
			String[] tempArr = new String[2];
			tempArr[0] = "-";
			tempArr[1] = "";
			listTimeTableSlots1.add(tempArr);
		}
	}
	
	for (int k = 0; k < listTimeTableSlots1.size(); k++)
	{
		
	}
		return listTimeTableSlots1;
	}
}