package org.vtop.CourseRegistration.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.model.WishlistRegistrationModel;
import org.vtop.CourseRegistration.model.WishlistRegistrationPKModel;
import org.vtop.CourseRegistration.repository.RegistrationLogRepository;
import org.vtop.CourseRegistration.repository.WishlistRegistrationRepository;
import org.vtop.CourseRegistration.service.AdditionalLearningCourseCatalogService;
import org.vtop.CourseRegistration.service.CourseCatalogService;
import org.vtop.CourseRegistration.service.CourseOptionService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseTypeComponentService;
import org.vtop.CourseRegistration.service.CourseTypeMasterService;
import org.vtop.CourseRegistration.service.EmployeeProfileService;
import org.vtop.CourseRegistration.service.RegistrationLogService;
import org.vtop.CourseRegistration.service.StudentHistoryService;
import org.vtop.CourseRegistration.service.WishlistRegistrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Controller
public class CourseRegistrationFormController 
{	
	@Autowired private CourseCatalogService courseCatalogService;
	
	@Autowired private RegistrationLogService registrationLogService;

	@Autowired private CourseTypeMasterService courseTypeMasterService;
	
	@Autowired private CourseTypeComponentService courseTypeComponentService;	

	@Autowired private StudentHistoryService studentHistoryService;

	@Autowired private AdditionalLearningCourseCatalogService additionalLearningCourseCatalogService;	
		
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	
	@Autowired private EmployeeProfileService employeeProfileService;
		
	@Autowired private RegistrationLogRepository registrationLogRepository;	
	
	@Autowired private CourseOptionService courseOptionService;
	
	@Autowired private WishlistRegistrationRepository wishlistRegistrationRepository;
	
	@Autowired private WishlistRegistrationService wishlistRegistrationService;
	
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationFormController.class);	
	private static final String[] classType = { "BFS" };
	private static final String RegErrorMethod = "SSS4EM2021WL";
	
	private static final String CAMPUSCODE = "CHN";	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 15, 20 };
	
	
	@PostMapping("viewRegistrationOption")
	public String viewRegistrationOption(Model model, HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) 
	{
		String IpAddress = (String) session.getAttribute("IpAddress");
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;		
		int allowStatus = 2, regularFlag = 2;
		@SuppressWarnings("unchecked")
		List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
		String programGroupCode = (String) session.getAttribute("ProgramGroupCode");
		regularFlag = (Integer) session.getAttribute("regularFlag");
		Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				
		model.addAttribute("regularFlag", regularFlag);
		model.addAttribute("PEUEAllowStatus", PEUEAllowStatus);
		
		try
		{
			int pageAuthStatus = 2;
			String pageAuthKey = "";
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				Integer maxCredit = (Integer) session.getAttribute("maxCredit");
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				int compulsoryStatus = 2;
				String registrationOption = "";
				Integer pageSize = 5;
				Integer page = 1;
				Integer searchType = 0;
				String searchVal = "";
				String subCourseOption = "";
				
				
				switch(allowStatus)
				{
					case 1:
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													programGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{
							registrationOption = "COMP";
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							session.setAttribute("registrationOption", registrationOption);
							
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							session.removeAttribute("registrationOption");
							
							model.addAttribute("studySystem", session.getAttribute("StudySystem"));
							model.addAttribute("regularFlag", session.getAttribute("regularFlag"));
							model.addAttribute("PEUEAllowStatus", PEUEAllowStatus);
							model.addAttribute("maxCredit", maxCredit);
							model.addAttribute("regularFlag", regularFlag);
							model.addAttribute("registrationMethod", session.getAttribute("registrationMethod"));
							model.addAttribute("ProgramGroupCode", programGroupCode);							
							model.addAttribute("showFlag", 0);
							
							urlPage = "mainpages/RegistrationOptionList::section";
						}
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
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"viewRegistrationOption", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;			
		}
		model.addAttribute("info", msg);
		return urlPage;
	}
	
	
	@PostMapping("processFFCStoCal")
	public String processFFCStoCal(Model model, HttpServletRequest request, HttpSession session) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, urlPage = "", infoMsg = "", subCourseOption ="",pageAuthKey = "";
		String registrationOption = "FFCSCAL";
		int allowStatus = 2,pageAuthStatus = 2;		
		Integer updateStatus = 1, page = 1;		
				
		try
		{	
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];		
				
				switch (allowStatus)
				{
					case 1:
						urlPage = processRegistrationOption(registrationOption, model, session, 5, page, 0, "NONE", 
									subCourseOption, request);
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
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processFFCStoCal", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			return urlPage;
		}		
		model.addAttribute("info", msg);
		return urlPage;
	}	
	

	@PostMapping("processRegistrationOption")
	public String processRegistrationOption(@RequestParam(value = "registrationOption", required = false) String registrationOption, 
						Model model, HttpSession session, @RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal,
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, HttpServletRequest request)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String flagValue = request.getParameter("flag");
		if ((flagValue == null) || (flagValue.equals(null)))
		{
			flagValue = "0";
		}
		
		String msg = null, infoMsg = "", urlPage = "";				
		Integer updateStatus = 1;
		int allowStatus = 2;
				
		if ((registrationOption != null) && (!registrationOption.equals(null))) 
		{
			session.setAttribute("registrationOption", registrationOption);
		} 
		else 
		{
			registrationOption = (String) session.getAttribute("registrationOption");
		}
		
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				List<String> courseRegWaitingList = new ArrayList<String>();
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];	
				
				int compulsoryStatus = 2;
				
				switch(allowStatus)
				{
					case 1:
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													ProgramGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{	
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							/*System.out.println(registrationOption +" | "+ pageSize +" | "+ page +" | "+ searchType +" | "+ searchVal);*/
							callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
							model.addAttribute("courseRegWaitingList", courseRegWaitingList);
							model.addAttribute("studySystem", session.getAttribute("StudySystem"));
							model.addAttribute("registrationOption", registrationOption);					
							model.addAttribute("showFlag", 1);
							
							switch (flagValue)
							{
								case "1":
									urlPage = "mainpages/CourseList :: cclistfrag";
									break;
								default:
									urlPage = "mainpages/CourseList :: section";
									break;
							}
						}
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
		catch (Exception ex) 
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processRegistrationOption", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		return urlPage;
	}
	
	public int callCourseRegistrationTypes(String registrationOption, Integer pageSize, Integer page, 
					Integer searchType, String searchVal, HttpSession session, Model model)
	{
		String semesterSubId = (String) session.getAttribute("SemesterSubId");		
		String registerNo = (String) session.getAttribute("RegisterNumber");
		
		try
		{
			if (semesterSubId != null)
			{
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer ProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer studYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");				
				String[] registerNumber = (String[]) session.getAttribute("registerNumberArray");				
				String registrationMethod = (String) session.getAttribute("registrationMethod");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
											
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
				//String srhLike = null;				
				
				/*System.out.println("pageSize: "+ pageSize +" | page: "+ page +" | pageSerialNo: "+ pageSerialNo 
						+" | evalPageSize: "+ evalPageSize +" | evalPage: "+ evalPage);*/
				
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				int totalPage = 0, pageNumber = evalPage; 
				String[] pagerArray = new String[]{};
				List<CourseCatalogModel> courseCatalogModelPageList = new ArrayList<CourseCatalogModel>();
							
				courseCatalogModelPageList = courseCatalogService.getCourseListForRegistration(registrationOption, 
												CAMPUSCODE, courseSystem, egbGroupId, programGroupId, semesterSubId, 
												ProgramSpecId, classGroupId, classType, studYear, curriculumVersion, 
												registerNo, srhType, srhVal, StudentGraduateYear, ProgramGroupCode, 
												ProgramSpecCode, registrationMethod, registerNumber, evalPage, 
												evalPageSize, costCentreCode);
								
				/*System.out.println("CourseListSize: "+ courseCatalogModelPageList.size() 
							+" | evalPageSize: "+ evalPageSize +" | pageNumber: "+ pageNumber);*/
				pagerArray = courseCatalogService.getTotalPageAndIndex(courseCatalogModelPageList.size(), 
								evalPageSize, pageNumber).split("\\|");
				totalPage = Integer.parseInt(pagerArray[0]);
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				//System.out.println("totalPage: "+ totalPage);
							
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("tlCourseCatalogModelList", courseCatalogModelPageList);
				/*model.addAttribute("courseRegModelList", courseRegistrationService.getRegisteredCourseByClassGroup(semesterSubId, 
						registerNo, classGroupId));*/
				model.addAttribute("courseRegModelList", wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
							semesterSubId, classGroupId, registerNo));
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}			
		}
		catch(Exception e)
		{
			session.invalidate();
		}
				
		return 1;
	}
	

	@PostMapping(value="processCourseRegistration")
	public String processCourseRegistration(String courseId, 
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						Model model, HttpSession session, HttpServletRequest request) 
	{			
		String IpAddress = (String) session.getAttribute("IpAddress");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String registerNumber = (String) session.getAttribute("RegisterNumber");
				
		String urlPage = "", courseTypeDisplay = "", msg = null, message = null, courseOption = "",	
					genericCourseType = "", infoMsg = "";
		String courseCategory = "NONE", subCourseType = "", subCourseDate = "", courseCode = "", 
					genericCourseTypeDisplay = "", authKeyVal = "", corAuthStatus = "", ccCourseId = "";
		String[] regStatusArr = new String[50];
		Integer updateStatus = 1;
		int allowStatus = 2, regStatusFlag = 2, projectStatus = 2, regAllowFlag = 1, wlAllowFlag = 1, 
				audAllowFlag = 1, rgrAllowFlag=2, minAllowFlag = 2, honAllowFlag = 2, adlAllowFlag = 2, 
				RPEUEAllowFlag=2,csAllowFlag=2,RUCUEAllowFlag=2;
		int ethExistFlag = 2, epjExistFlag = 2, epjSlotFlag = 2,regularFlag=2;
				
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{			
			if ((semesterSubId != null) && (pageAuthStatus == 1))
			{						
				registerNumber = (String) session.getAttribute("RegisterNumber");					
				String[] pCourseSystem = (String[]) session.getAttribute("StudySystem");
				Integer pProgramGroupId = (Integer) session.getAttribute("ProgramGroupId"); 
				String pProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer pProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				
				String pProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String pSemesterSubId = (String) session.getAttribute("SemesterSubId"); 
				Integer semesterId = (Integer) session.getAttribute("SemesterId");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
				Integer maxCredit = (Integer) session.getAttribute("maxCredit");
				
				String registrationOption = (String) session.getAttribute("registrationOption");
				String subCourseOption = (String) session.getAttribute("subCourseOption");
				Integer StudyStartYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer acadGraduateYear = (Integer) session.getAttribute("acadGraduateYear");
				
				Integer OptionNAStatus=(Integer) session.getAttribute("OptionNAStatus");
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				String studentStudySystem = (String) session.getAttribute("studentStudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				String studentCgpaData = (String) session.getAttribute("studentCgpaData");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				//Integer eoAllowStatus = (Integer) session.getAttribute("eoAllowStatus");
				//Integer giAllowStatus = (Integer) session.getAttribute("giAllowStatus");
				
				regularFlag = (Integer) session.getAttribute("regularFlag");
				session.setAttribute("corAuthStatus", "NONE");
				session.setAttribute("authStatus", "NONE");

				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
					
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<String> courseTypeArr = new ArrayList<String>();					
				@SuppressWarnings("unchecked")
				List<String> registerNumberList = (List<String>) session.getAttribute("registerNumberList");
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				
				CourseCatalogModel courseCatalog = new CourseCatalogModel();
				/*List<CourseAllocationModel> list1 = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> ela = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> epj = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> courseAllocationList = new ArrayList<CourseAllocationModel>();*/
				
				courseCatalog = courseCatalogService.getOne(courseId);
				if (courseCatalog != null)
				{
					courseCode = courseCatalog.getCode();
					genericCourseType = courseCatalog.getGenericCourseType();
					genericCourseTypeDisplay = courseCatalog.getCourseTypeComponentModel().getDescription();
				}
					
				/*System.out.println(pCourseSystem +" | "+ pProgramGroupId +" | "+ pProgramGroupCode +" | "+
						pProgramSpecCode +" | "+ pSemesterSubId +" | "+ registerNumber +" | "+ 
						pOldRegisterNumber +" | "+ maxCredit +" | "+ courseId +" | "+ StudyStartYear+" | "+
						StudentGraduateYear +" | "+ studentStudySystem +" | "+ pProgramSpecId +" | "+ CurriculumVersion +" | "+
						PEUEAllowStatus +" | "+ programGroupMode +" | "+ classGroupId +" | "+ studentCgpaData +" | "+ 
						OptionNAStatus +" | "+ compCourseList +" | "+ semesterId +" | "+ costCentreCode +" | "+ acadGraduateYear);*/
				
				
				switch(allowStatus)
				{
					case 1:
						regStatusArr = courseRegCommonFn.CheckRegistrationCondition(pCourseSystem, pProgramGroupId, 
											pProgramGroupCode, pProgramSpecCode, pSemesterSubId, registerNumber, 
											pOldRegisterNumber, maxCredit, courseId, StudyStartYear, StudentGraduateYear, 
											studentStudySystem, pProgramSpecId, CurriculumVersion, PEUEAllowStatus,
											programGroupMode, classGroupId, studentCgpaData,OptionNAStatus,compCourseList,
											semesterId,costCentreCode,acadGraduateYear).split("/");
						regStatusFlag = Integer.parseInt(regStatusArr[0]);
						message = regStatusArr[1];							
						courseOption = regStatusArr[2];
						regAllowFlag = Integer.parseInt(regStatusArr[3]);
						wlAllowFlag = Integer.parseInt(regStatusArr[4]);
						audAllowFlag = Integer.parseInt(regStatusArr[8]);
						rgrAllowFlag= Integer.parseInt(regStatusArr[11]);
						minAllowFlag = Integer.parseInt(regStatusArr[13]);
						honAllowFlag = Integer.parseInt(regStatusArr[12]);
						courseCategory = regStatusArr[14];
						adlAllowFlag = Integer.parseInt(regStatusArr[15]);
						authKeyVal = regStatusArr[16];
						RPEUEAllowFlag = Integer.parseInt(regStatusArr[17]);
						csAllowFlag = Integer.parseInt(regStatusArr[18]);
						RUCUEAllowFlag = Integer.parseInt(regStatusArr[19]);
						ccCourseId = regStatusArr[20];
						
						corAuthStatus = regStatusArr[2] +"/"+ regStatusArr[3] +"/"+ regStatusArr[4] 
											+"/"+ regStatusArr[8] +"/"+ regStatusArr[11] +"/"+ regStatusArr[13] 
											+"/"+ regStatusArr[12] +"/"+ regStatusArr[14] +"/"+ regStatusArr[15] 
											+"/"+ regStatusArr[17] +"/"+ regStatusArr[6] +"/"+ regStatusArr[7] 
											+"/"+ regStatusArr[9] +"/"+ regStatusArr[10] +"/"+ regStatusArr[18] 
											+"/"+ regStatusArr[19] +"/"+ regStatusArr[20];
			
						session.setAttribute("authStatus", authKeyVal);
						session.setAttribute("corAuthStatus", corAuthStatus);
												
						//System.out.println("corAuthStatus: "+ corAuthStatus);
						//System.out.println("AuthKeyVal: "+ authKeyVal);
						
						/*if(PEUEStatusFlag == 1)
						{*/
							switch(courseOption)
							{
								case "RR":
								case "RRCE":									
									if (!regStatusArr[6].equals("NONE"))
									{
										courseTypeArr = Arrays.asList(regStatusArr[6].split(","));
									}																	
									
									if (courseTypeArr.size() <= 0)
									{
										courseTypeArr = courseTypeMasterService.getCourseTypeComponent(genericCourseType);
									}
									break;
								
								default:
									courseTypeArr = courseTypeMasterService.getCourseTypeComponent(genericCourseType);
									break;
							}
								
							switch(courseOption)
							{
								case "RR":
								case "RRCE":
								case "GI":
								case "GICE":
								case "RGCE":
								case "RPCE":
								case "RWCE":
									subCourseOption = regStatusArr[7];
									subCourseType = regStatusArr[9];
									subCourseDate = regStatusArr[10];
									break;
								default:
									if (regStatusArr[7].equals("NONE"))
									{
										subCourseOption = "";
									}
									break;
							}
								
							for (String crstp : courseTypeArr) 
							{
								if (courseTypeDisplay.equals(""))
								{
									courseTypeDisplay = courseTypeComponentService.getOne(crstp).getDescription();
								}
								else
								{
									courseTypeDisplay = courseTypeDisplay +" / "+ 
															courseTypeComponentService.getOne(crstp).getDescription();
								}
									
								if (crstp.equals("ETH"))
								{
									ethExistFlag = 1;
								}
								else if (crstp.equals("EPJ"))
								{
									epjExistFlag = 1;
								}								 
							}
								
							if ((courseTypeArr.size() == 2) && (genericCourseType.equals("ETLP")) 
									&& (ethExistFlag == 1) && (epjExistFlag == 1))
							{
								epjSlotFlag = 1;
							}
							else if ((courseTypeArr.size() == 1) && (epjExistFlag == 1))
							{
								epjSlotFlag = 1;
							}
															
							switch(regStatusFlag)
							{    
								case 1:								
									if (projectStatus == 1)
									{
										List<Object[]> courseCostCentre = employeeProfileService.getFacultyCostCentre(CAMPUSCODE);
										
										model.addAttribute("courseCostCentre", courseCostCentre);
										model.addAttribute("ProgramCode", session.getAttribute("ProgramGroupCode"));
										model.addAttribute("courseOption", courseOption);										
										
										urlPage = "mainpages/ProjectRegistration :: section";
									}
									else
									{
										urlPage = "mainpages/CourseRegistration :: section";
									}
										
									model.addAttribute("shcssList", studentHistoryService.getStudentHistoryCS2(registerNumberList, 
												courseCode, studentStudySystem, pProgramSpecId, StudyStartYear, curriculumVersion, 
												semesterSubId, courseCategory, courseOption, ccCourseId, classGroupId));
									model.addAttribute("minorList", additionalLearningCourseCatalogService.getTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												minAllowFlag, "MIN", pProgramGroupId, pProgramSpecId, courseCode,studentStudySystem));
									model.addAttribute("honorList", additionalLearningCourseCatalogService.getTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												honAllowFlag, "HON", pProgramGroupId, pProgramSpecId, courseCode,studentStudySystem));
									model.addAttribute("courseOptionList",courseOptionService.getRegistrationCourseOption(
												courseOption, genericCourseType, rgrAllowFlag, audAllowFlag, honAllowFlag, 
												minAllowFlag, adlAllowFlag, csAllowFlag, RPEUEAllowFlag, RUCUEAllowFlag));
									/*model.addAttribute("tlClashMapList", courseRegCommonFn.getClashSlotStatus(semesterSubId, 
												registerNumber, courseAllocationList));*/
									
									
									model.addAttribute("regAllowFlag", regAllowFlag);
									model.addAttribute("wlAllowFlag", wlAllowFlag);
									model.addAttribute("epjSlotFlag", epjSlotFlag);
									model.addAttribute("rgrAllowFlag", rgrAllowFlag);
									model.addAttribute("minAllowFlag", minAllowFlag);
									model.addAttribute("honAllowFlag", honAllowFlag);
									model.addAttribute("RPEUEAllowFlag", RPEUEAllowFlag);
									model.addAttribute("csAllowFlag", csAllowFlag);
									model.addAttribute("RUCUEAllowFlag", RUCUEAllowFlag);
									model.addAttribute("courseCatalogModel", courseCatalog);
									model.addAttribute("page", page);
									model.addAttribute("srhType", searchType);
									model.addAttribute("srhVal", searchVal);
									model.addAttribute("courseOption", courseOption);
									model.addAttribute("registrationOption", registrationOption);						
									model.addAttribute("subCourseOption", subCourseOption);
									model.addAttribute("audAllowFlag", audAllowFlag);
									model.addAttribute("adlAllowFlag", adlAllowFlag);
									model.addAttribute("tlcourseType", courseTypeArr);					
									model.addAttribute("courseTypeDisplay", courseTypeDisplay);
									model.addAttribute("genericCourseTypeDisplay", genericCourseTypeDisplay);
									model.addAttribute("ProgramGroupCode", pProgramGroupCode);
									model.addAttribute("subCourseType", subCourseType);
									model.addAttribute("subCourseDate", subCourseDate);
									model.addAttribute("regularFlag", regularFlag);	
									model.addAttribute("tlCourseCategory", courseCategory);
									model.addAttribute("tlCompCourseList", compCourseList);
																		
									break;  
										
								case 2:
									model.addAttribute("infoMessage", message);									
									urlPage = processRegistrationOption(registrationOption, model, session, 5, page, searchType, 
													searchVal, subCourseOption, request);									
									break;  
							}					
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
		catch(Exception ex)
		{
			//System.out.println(ex);
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processCourseRegistration", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		
		return urlPage;
	}
	
	
	
	
	@PostMapping(value = "processRegisterCourse")
	public String processRegisterCourse(String ClassID, String courseId, String courseType, String courseCode, 
							String courseOption, String clashSlot, String epjSlotFlag, 
							@RequestParam(value = "pageSize", required = false) Integer pageSize, 
							@RequestParam(value = "page", required = false) Integer page,
							@RequestParam(value = "searchType", required = false) Integer searchType, 
							@RequestParam(value = "searchVal", required = false) String searchVal,
							@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
							@RequestParam(value = "subCourseType", required = false) String subCourseType,
							@RequestParam(value = "subCourseDate", required = false) String subCourseDate,
							Model model, String[] clArr, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String registrationOption = (String) session.getAttribute("registrationOption");
		String IpAddress = (String) session.getAttribute("IpAddress");
		String msg = null, infoMsg = "", urlPage = "", message = null;		
		Integer allowStatus = 2,updateStatus = 1;
		
		try
		{
			String authStatus = (String) session.getAttribute("authStatus");
			int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
			int pageAuthStatus = 2,chkFlag = 2, count = 0, courseTypeFlag = 2;
			String pageAuthKey = "";
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
			String[] equvCourseTypeArray = new String[]{};
			
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			/*System.out.println("authCheckStatus: "+ authCheckStatus +" | registerNumber: "+ registerNumber
					+" | pageAuthStatus: "+ pageAuthStatus);*/
			
			if((authCheckStatus == 1) && (registerNumber!=null) && (pageAuthStatus == 1) )
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
									
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				CourseCatalogModel ccm = new CourseCatalogModel();				
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];		
							
				ccm = courseCatalogService.getOne(courseId);

				int compulsoryStatus = 2;
				List<String> courseTypeList = new ArrayList<String>();
				List<String> regCourseTypeList = new ArrayList<String>();

				switch (allowStatus)
				{
					case 1:
						if ((ccm != null) && (clArr.length > 0))
						{
							int typeCheckFlag = 1;
							courseTypeList = courseTypeMasterService.getCourseTypeComponent(ccm.getGenericCourseType());
							
							for (int i = 0; i<clArr.length; i++)
							{
								if (courseTypeList.contains(clArr[i]))
								{
									regCourseTypeList.add(clArr[i]);
									//System.out.println("Reg. CourseType: "+ clArr[i]);
								}
								else
								{
									typeCheckFlag = 2;
									break;
								}
							}
							
							if (typeCheckFlag == 1)
							{
								chkFlag = 1;
							}
							else
							{
								msg = "Invalid details.";
							}
						}
						else
						{
							msg = "Invalid details.";
						}
						
						if (chkFlag == 1)
						{
							WishlistRegistrationModel wishlistRegistrationModel = new WishlistRegistrationModel();
							WishlistRegistrationPKModel wishlistRegistrationPKModel = new WishlistRegistrationPKModel();
							
							
							if ((!subCourseOption.equals("")) && (!subCourseOption.equals(null)))
							{
								switch(courseOption)
								{
									case "RR":
									case "RRCE":
									case "GI":
									case "GICE":
									case "RGCE":
									case "RPCE":
									case "RWCE":
										equvCourseTypeArray = subCourseType.split("\\,");
										courseTypeFlag = 1;
										break;
									case "CS":
										String[] subCrsOptArr = subCourseOption.split("/");
										subCourseOption = subCrsOptArr[0];
										subCourseType = subCrsOptArr[1];
										subCourseDate = subCrsOptArr[2];
										break;
									default:
										subCourseType = "";
										subCourseDate = "";
										break;
								}
							}
														
							if (regCourseTypeList.size() > 0)
							{	
								synchronized (this)
								{									
									for (String crsType: regCourseTypeList)
									{
										wishlistRegistrationPKModel.setClassGroupId(classGroupId[0]);
										wishlistRegistrationPKModel.setCourseId(courseId);
										wishlistRegistrationPKModel.setCourseType(crsType);
										wishlistRegistrationPKModel.setRegisterNumber(registerNumber);
										wishlistRegistrationPKModel.setSemesterSubId(semesterSubId);
										wishlistRegistrationModel.setWlRegPKId(wishlistRegistrationPKModel);
										wishlistRegistrationModel.setCourseOptionCode(courseOption);
										wishlistRegistrationModel.setComponentType(0);
										wishlistRegistrationModel.setLogUserId(registerNumber);
										wishlistRegistrationModel.setLogIpaddress(IpAddress);
										wishlistRegistrationModel.setLogTimestamp(new Date());
										
										if (subCourseOption != null)
										{
											wishlistRegistrationModel.setEquivalanceCourseId(subCourseOption);
										}
										
										if (subCourseType != null)
										{
											if (courseTypeFlag == 1)
												wishlistRegistrationModel.setEquivalanceCourseType(equvCourseTypeArray[count]);
											else
												wishlistRegistrationModel.setEquivalanceCourseType(subCourseType);
										}
										
										if ((subCourseDate != null) && (!subCourseDate.equals("")) && (!subCourseDate.equals("NONE")))
										{
											wishlistRegistrationModel.setEquivalanceExamMonth(format.parse(subCourseDate));
										}
										
										wishlistRegistrationRepository.save(wishlistRegistrationModel);
										count++;
									}							
								}						
							}
							
							msg = "Registered Successfully.";
						}
											
							session.setAttribute("authStatus", "NONE");
														
							
							if (compulsoryCourseStatus == 1)
							{
								compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
														StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
														classGroupId, classType, ProgramSpecCode, programSpecId, 
														ProgramGroupCode, pOldRegisterNumber, compCourseList, 
														costCentreCode);
								session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
							}
							
							if (compulsoryStatus == 1)
							{
								getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
										subCourseOption, session, model, compCourseList);
								model.addAttribute("info", msg);
								urlPage = "mainpages/CompulsoryCourseList :: section";
							}
							else
							{
								if (registrationOption.equals("COMP"))
								{
									session.removeAttribute("registrationOption");
									model.addAttribute("studySystem", session.getAttribute("StudySystem"));
									model.addAttribute("regularFlag", session.getAttribute("regularFlag"));
									model.addAttribute("registrationMethod", session.getAttribute("registrationMethod"));
									model.addAttribute("showFlag", 0);
									model.addAttribute("info", msg);
									urlPage = "mainpages/RegistrationOptionList :: section";
								}
								else
								{
									callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
									model.addAttribute("info", msg);
									List<String> courseRegModelList = wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
																			semesterSubId, classGroupId, registerNumber);
									model.addAttribute("courseRegModelList", courseRegModelList);
									urlPage = "mainpages/CourseList :: section";
								}
							}
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
		catch(Exception ex)
		{
			/*logger.error(ex);
			logger.trace("Regno: "+ registerNumber +" | Message: "+ message 
					+ " | msg: " + msg + " | pClassIdArr: " + pClassIdArr 
					+ " | pCompTypeArr: "+ pCompTypeArr +" | registrationOption: " + registrationOption);*/
						
			session.setAttribute("authStatus", "NONE");
			model.addAttribute("flag", 1);
			
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processRegisterCourse", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			
			urlPage = "redirectpage";
			return urlPage;
		}
				
		model.addAttribute("infoMessage", message);
		return urlPage;		
	}
	

	@PostMapping("processSearch")
	public String processSearch(Model model, HttpSession session, 
						@RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page, 
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
						HttpServletRequest request) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;
		int allowStatus = 2;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		//List<String> courseRegWaitingList = new ArrayList<String>();
		
		
		try 
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{	
				String registrationOption = (String) session.getAttribute("registrationOption");				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
					
				switch (allowStatus)
				{
					case 1:
						callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, 
								session, model);
						model.addAttribute("registrationOption", registrationOption);
						model.addAttribute("searchFlag", 1);
						urlPage = "mainpages/CourseList::section";						
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
		catch (Exception ex) 
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processSearch", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";			
			return urlPage;
		}
		return urlPage; 
	}
	
	public int getCompulsoryCourseList(String registrationOption, Integer pageSize, Integer page, Integer searchType,
						String searchVal, String subCourseOption, HttpSession session, Model model, List<String> courseCode)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");		
		Pager pager = null;
		int evalPageSize = INITIAL_PAGE_SIZE;
		int evalPage = INITIAL_PAGE;
		evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
		evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
		int pageSerialNo = evalPageSize * evalPage;
		int srhType = (searchType == null) ? 0 : searchType;
		String srhVal = (searchVal == null) ? "NONE" : searchVal;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer ProgramGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				Page<CourseCatalogModel> courseCatalogModelPageList = null;
								
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				List<String> courseRegModelList = wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
														semesterSubId, classGroupId, registerNumber);
								
				if (srhType == 0)
				{
					courseCatalogModelPageList = courseCatalogService.getCompulsoryCoursePagination(CAMPUSCODE, courseSystem, 
														egbGroupId, ProgramGroupId.toString(), semesterSubId, classGroupId, 
														classType, courseCode, ProgramGroupCode, ProgramSpecCode, costCentreCode, 
														new PageRequest(evalPage, evalPageSize));
					pager = new Pager(courseCatalogModelPageList.getTotalPages(), courseCatalogModelPageList.getNumber(), 
									BUTTONS_TO_SHOW);
				}
				
				if (courseCatalogModelPageList != null)
				{
					model.addAttribute("courseCatalogModelPageList", courseCatalogModelPageList);
				}
				
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("courseRegModelList", courseRegModelList);			
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}
			
		}
		catch(Exception ex)
		{
			logger.catching(ex);
		}
		return 1;
	}
	
	@PostMapping(value="processPageNumbers")
	public String processPageNumbers(Model model, HttpSession session, HttpServletRequest request, 
						@RequestParam(value="pageSize", required=false) Integer pageSize,
						@RequestParam(value="page", required=false) Integer page, 
						@RequestParam(value="searchType", required=false) Integer searchType, 
						@RequestParam(value="searchVal", required=false) String searchVal, 
						@RequestParam(value="totalPage", required=false) Integer totalPage, 
						@RequestParam(value="processType", required=false) Integer processType)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		String urlPage = "";
		
		//System.out.println("registerNumber: "+ registerNumber +" | IpAddress: "+ IpAddress);
		//System.out.println("pageSize: "+ pageSize +" | page: "+ page +" | searchType: "+ searchType 
		//		+" | searchVal: "+ searchVal +" | totalPage: "+ totalPage +" | processType: "+ processType);
		try
		{
			if (registerNumber != null)
			{				
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
				
				int pageNumber = evalPage;
				
				if (pageNumber <= 0)
				{
					pageNumber = 0;
				}
				else if ((int)pageNumber >= (int)totalPage)
				{
					pageNumber = totalPage - 1;
				}
				
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
				
				//System.out.println("totalPage: "+ totalPage +" | pageNumber: "+ pageNumber);
				//System.out.println("pageSerialNo: "+ pageSerialNo +" | evalPageSize: "+ evalPageSize 
				//		+" | srhType: "+ srhType +" | srhVal: "+ srhVal +" | pager: "+ pager 
				//		+" | page: "+ page);
				
				if (processType == 1)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag";
				}
				else if (processType == 2)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag2";
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
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processPageNumbers", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}		

		return urlPage;
	}
}