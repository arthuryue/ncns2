package hksarg.jud.ncns.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hksarg.jud.ncns.model.ApiError;
import hksarg.jud.ncns.model.Group;
import hksarg.jud.ncns.model.Judgm;
import hksarg.jud.ncns.model.JudgmAplFr;
import hksarg.jud.ncns.model.JudgmCase;
import hksarg.jud.ncns.model.JudgmCaseTitle;
import hksarg.jud.ncns.model.JudgmXfr;
import hksarg.jud.ncns.model.NcnCasePrefix;
import hksarg.jud.ncns.model.NcnLast;
import hksarg.jud.ncns.model.Response;
import hksarg.jud.ncns.model.NcnUser;
import hksarg.jud.ncns.model.dataInterface.CaseNo;
import hksarg.jud.ncns.model.dataInterface.UserMenu;
import hksarg.jud.ncns.model.dataInterface.UserPermissions;
import hksarg.jud.ncns.respository.JudgmRepository;
import hksarg.jud.ncns.security.NcnUserDetailsService;
import hksarg.jud.ncns.service.JudgmCaseService;
import hksarg.jud.ncns.service.JudgmService;
import hksarg.jud.ncns.service.NcnCasePrefixService;
import hksarg.jud.ncns.service.NcnLastService;
import hksarg.jud.ncns.service.NcnService;
import hksarg.jud.ncns.service.NcnUserService;
import hksarg.jud.ncns.service.Impl.NcnServiceImpl;
//import lombok.extern.slf4j.Slf4j;
import lombok.NoArgsConstructor;

import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import java.io.File;

import java.security.Principal;

@RestController
@RequestMapping("/ncns")
public class MaintenanceController {
	@Autowired
	private Environment env;
	@Autowired
	private NcnService ncnService;
	@Autowired
	private NcnServiceImpl ncnServiceImpl;
	@Autowired
	private JudgmService judgmService;
	@Autowired
	private JudgmCaseService judgmCaseService;
	@Autowired
	private NcnLastService ncnLastService;
	@Autowired
	private JudgmRepository judgmRepository;
	@Autowired
	private NcnUserService ncnUserService;
	// local
	@Autowired
	protected NcnUserDetailsService ncnUserDetailService;

//	@Autowired
//	private ModelMapper mapper;

	// static Logger log = Logger.getLogger(NcnController.class);

	// public MaintenanceController(HttpServletRequest request) {
//	public  MaintenanceController(HttpServletRequest request) {
//        Principal principal = request.getUserPrincipal();
//        
//        this.loginAdUserName = principal.getName();
//        System.out.println("loginAdUserName: " + this.loginAdUserName);
//    }

	private final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

	@GetMapping(path = "/searchcaseno/{part_caseno}")
	public ResponseEntity<Response> searchCaseNo(@PathVariable(name = "part_caseno") String part_caseno)
	// (@PathVariable(name = "judgmId") Long judgmId)
	{
		List<CaseNo> c = ncnService.searchCaseNo(part_caseno);
		String message = null;

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(of("caseno", (c == null ? "" : c)))
				.message(message).status(OK).statusCode(OK.value()).build());

	}

	@GetMapping(path = "/gotoJudgm/{judgmId}")
	public RedirectView gotoJudgm(@PathVariable(name = "judgmId") Long judgmId, RedirectAttributes attributes)
	// (@PathVariable(name = "judgmId") Long judgmId)
	{
		// ModelAndView mav = new ModelAndView("maintenance");

		Judgm j = judgmService.getJudgm(judgmId);

		attributes.addAttribute("gotoNcnCrt", j.getNcnCrt());
		attributes.addAttribute("gotoNcnNo", j.getNcnNo());
		attributes.addAttribute("gotoNcnYr", j.getNcnYr());
		// attributes.addAttribute("userCasePrefixs", listCasePrefixs);
		// attributes.addAttribute("courtListStr", courtListStr);

		// Check if revoked or confirmed
		// if (j.getStatus()=="I" ||
		// j.getJudgmXfrs().get(0).getConfirmDate() != null)

		// return new RedirectView ("/ncn/maintenance.readonly");

		return new RedirectView("/ncns/maintab");
		// return new RedirectView("/ncns/maintab",true,false);
	};

	@GetMapping(path = "/getJudgm/{startDate}/{endDate}/{status}")
	public ResponseEntity<List<Judgm>> queryJudgmByJudgeDate(@PathVariable(name = "startDate") LocalDateTime startDate,
			@PathVariable(name = "endDate") LocalDateTime endDate, @PathVariable(name = "status") String status) {

		return ResponseEntity.ok(judgmService.getJudgmByDatesAndStatus(startDate, endDate, status));

	};

	@PostMapping(path = "/getJudgm")
	public ResponseEntity<List<Judgm>> queryJudgmByJudgeDate(@RequestBody String json) throws Exception {

		// {"startDate":"2001/01/01","endDate":"2022/12/14","status":"A"}

		JsonNode jnode = new ObjectMapper().readTree(json);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

		String startDateText = jnode.get("startDate").asText() == "" ? "1900/01/01" : jnode.get("startDate").asText();
		String endDateText = jnode.get("endDate").asText() == "" ? "2999/12/01" : jnode.get("endDate").asText();

		LocalDateTime startDate = LocalDate.parse(startDateText, formatter).atTime(0, 0);
		LocalDateTime endDate = LocalDate.parse(endDateText, formatter).atTime(23, 59, 59);

		String status = jnode.get("status").asText();
		String isRestricted = jnode.get("restricted").asText();

		// Public
		if (isRestricted.equals("N")) {
			// confirmed
			if (status.equals("C"))
				return ResponseEntity.ok(judgmService.getJudgmConfirmed(startDate, endDate));
			// uploaded
			else if (status.equals("U"))
				return ResponseEntity.ok(judgmService.getJudgmUploaded(startDate, endDate));
			else if (status.equals("A") || status.equals("I"))
			{
//				List<Judgm> jtest = judgmService.getJudgmByDatesAndStatus(startDate, endDate, status);
//				List<Judgm> yes = new ArrayList<Judgm>();
//				yes.add(new Judgm());
//				return ResponseEntity.ok(yes);	
				return ResponseEntity
				.ok(judgmService.getJudgmByDatesAndStatus(startDate, endDate, status));
//				return ResponseEntity
//				.ok(judgmService.getJudgmByDatesAndStatus2(startDate, endDate, status));
				
			}
				

			else if (status.equals(""))
				return ResponseEntity
						.ok(judgmService.getAllJudgmByDate(startDate, endDate));
			return null;
		} else
			return ResponseEntity.ok(judgmService.getJudgmByDatesAndStatusAndRestricted(status == "" ? null : status));

	};

	@GetMapping("/thymeleaf/judgmForm")
	public ModelAndView showUpdateForm(@RequestParam Long id, Model model) {
		ModelAndView mav = new ModelAndView("update-judgm-form");
		Judgm j = judgmRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
		mav.addObject("judgm", j);
		return mav;
	}

	@GetMapping("/enquiry")
	public ModelAndView enquiry(Principal principal) {

		String userName = principal.getName(); // local
		// String userName =
		// SecurityContextHolder.getContext().getAuthentication().getName();

		List<UserPermissions> userPermissions = ncnUserService.getUserPermissons(userName);
		List<UserMenu> listUserMenu = ncnUserService.getUserMenu(userPermissions);
		List<UserPermissions> listFuncCourts = ncnUserService.getUserFuncCourts(userPermissions, "MOD002");

		String courtListStr = ncnUserService.getCasePrefixStr(listFuncCourts);

		System.out.println(listFuncCourts);

		ModelAndView mav = new ModelAndView("enquiry");
		mav.addObject("menuTitle", env.getProperty("app.menuTitle"));
		mav.addObject("userMenu", listUserMenu);
		mav.addObject("currentfunc", "MOD002");
		mav.addObject("funcCourts", listFuncCourts);
		mav.addObject("courtListStr", courtListStr);
		mav.addObject("ncnYears", ncnService.getNcnYears());

		return mav;
	}

	@GetMapping(value = { "/main/restricted" })
	public ModelAndView maintenanceRestricted(Principal principal) {

		// Get User
		// String userName = principal.getName(); // local
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();

		// NcnUser ncnUser = ncnUserService.findByNameWithRoles(userName);

		// Judgm j = judgmRepository.findById(id).orElseThrow(() -> new
		// IllegalArgumentException("Invalid Id:" + id));
		// mav.addObject("judgm", j);

		List<UserPermissions> userPermissions = ncnUserService.getUserPermissons(userName);
		List<UserMenu> listUserMenu = ncnUserService.getUserMenu(userPermissions);
		List<UserPermissions> listFuncCourts = ncnUserService.getUserFuncCourts(userPermissions,
				new ArrayList<>(Arrays.asList("MOD003")));
		String courtListStr = ncnUserService.getCasePrefixStr(listFuncCourts);

		ModelAndView mav = new ModelAndView("maintenance-tabulator-restricted");
		mav.addObject("menuTitle", env.getProperty("app.menuTitle"));
		mav.addObject("userMenu", listUserMenu);
		mav.addObject("currentfunc", "MOD003");
		mav.addObject("funcCourts", listFuncCourts);
		mav.addObject("courtListStr", courtListStr);
		mav.addObject("ncnYears", ncnService.getNcnYears());

		return mav;
	}

	@GetMapping(value = { "/main" })
	public ModelAndView maintenance(Principal principal) {

		// Get User
		String userName = principal.getName();
		// String userName =
		// SecurityContextHolder.getContext().getAuthentication().getName();

		// Judgm j = judgmRepository.findById(id).orElseThrow(() -> new
		// IllegalArgumentException("Invalid Id:" + id));
		// mav.addObject("judgm", j);

		List<UserPermissions> userPermissions = ncnUserService.getUserPermissons(userName);
		List<UserMenu> listUserMenu = ncnUserService.getUserMenu(userPermissions);
		List<UserPermissions> listFuncCourts = ncnUserService.getUserFuncCourts(userPermissions,
				new ArrayList<>(Arrays.asList("MOD000")));
		String courtListStr = ncnUserService.getCasePrefixStr(listFuncCourts);

		for (UserPermissions t : userPermissions) {
			System.out.println(t.getOwnerId());
		}
		System.out.println("listFuncCourts");
		for (UserPermissions t : listFuncCourts) {
			System.out.println(t.getOwnerId());
		}
		ModelAndView mav = new ModelAndView("maintenance-tabulator");
		mav.addObject("menuTitle", env.getProperty("app.menuTitle"));
		mav.addObject("userMenu", listUserMenu);
		mav.addObject("currentfunc", "MOD000");
		mav.addObject("funcCourts", listFuncCourts);
		mav.addObject("courtListStr", courtListStr);
		mav.addObject("ncnYears", ncnService.getNcnYears());

//		LocalDate today = LocalDate.now();
//		LocalDate dateNov = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1);
//		System.out.println("Current Date="+today);
//		System.out.println("Nov Date="+dateNov);
//		
//		if (today.isAfter(dateNov))
//				{
//			System.out.println("Next Year"+(LocalDate.now().getYear() + 1));
//				}

		return mav;
	}

	@PostMapping("/thymeleaf/saveJudgm")
	public ModelAndView saveJudgm(@RequestPart(value = "test_file", required = false) MultipartFile file,
			@RequestPart(value = "judgmId", required = false) String judgmId, @ModelAttribute Judgm judgm)
			throws Exception {

//			@RequestParam("judgmId") String id,
//			@RequestParam("file") MultipartFile file)
//	{

//		if (result.hasErrors()) {
//	        user.setId(id);
//	        return "update-user";
//	    }
		Optional<Judgm> j = judgmRepository.findById(Long.parseLong(judgmId));

		if (j.isPresent()) {
			// Judgm j = j
			Judgm j0 = j.get();
			j0.setDocType(judgm.getDocType());
			judgmRepository.save(j0);
			// operate on existingCustomer
		} else {
			// there is no Customer in the repo with 'id'
		}

		// Optional<Judgm> judgm0 = judgmRepository.findById(Long.parseLong(judgmId));
		// judgm0.set

//		/judgm.setJudgmId(Long.parseLong(judgmId));
		log.info("judgm: " + judgm.toString());
		log.info("getNcnCrt: " + judgm.getNcnCrt());
		log.info("getCreateBy: " + judgm.getCreateBy());
		log.info("judgmId: " + judgmId);

		// judgm.setJudgmId(judgmId);

		return new ModelAndView("redirect:/ncns/thymeleaf/judgmForm?id=" + judgmId);

	}

	@PostMapping(path = "/confirm")
	public ResponseEntity<Response> confirmNcn(@RequestBody String json) throws Exception {

		// return ResponseEntity.ok(judgmService.getJudgmByDatesAndStatus(startDate,
		// endDate, status));
		JsonNode jnode = new ObjectMapper().readTree(json);
		// int judgmId = jnode.get("judgmId").intValue();
		int judgmId = Integer.parseInt(jnode.get("judgmId").asText());

		Judgm j = judgmService.confirmJudgm(judgmId);

		JSONObject dataObj = new JSONObject();
		dataObj.put("judgm", (j == null ? "" : j));

		return ResponseEntity
				.ok(Response.builder().timeStamp(now()).data(dataObj).status(OK).statusCode(OK.value()).build());
	};

	@PutMapping(path = "/revoke")
	public ResponseEntity<Response> remokeNcn(@RequestBody String json, Principal principal) throws Exception {

		String userName = principal.getName();
		
		JsonNode jnode = new ObjectMapper().readTree(json);
		// int judgmId = jnode.get("judgmId").intValue();
		int judgmId = Integer.parseInt(jnode.get("judgmId").asText());

//		JsonNode casesNodes = judgmNode.at("/vjudgmCases");

		log.info("[judgmId] :" + String.valueOf(judgmId));

		Judgm j = judgmService.revokeJudgm(judgmId, userName);

		System.out.println(j);

		// return ResponseEntity.ok(j);
		JSONObject dataObj = new JSONObject();
		dataObj.put("judgm", (j == null ? "" : j));

		return ResponseEntity
				.ok(Response.builder().timeStamp(now()).data(dataObj).status(OK).statusCode(OK.value()).build());

	}

	@GetMapping(path = "/ncnByCase/apl/{casePrefix}/{caseType}/{caseSer}/{caseYear}")
	public ResponseEntity<List<JudgmAplFr>> getNcnsByCaseApl(@PathVariable(name = "casePrefix") String casePrefix,
			@PathVariable(name = "caseType") String caseType,
			@PathVariable(name = "caseSer", required = false) String caseSer,
			@PathVariable(name = "caseYear", required = false) String caseYear, HttpServletRequest request)
			throws EntityNotFoundException {

		String case_no = String.format("%1$s%2$s%3$s/%4$s", casePrefix, caseType, caseSer, caseYear);

		log.info("Entered case# : " + case_no);
		System.out.println(request.getRequestURI());

		List<JudgmAplFr> jlst = judgmService.getJudgmAplFrByCase(casePrefix, caseType, Integer.parseInt(caseSer),
				Integer.parseInt(caseYear));

//		List<Judgm> judgms = judgmService.getJudgmByCases(casePrefix, caseType, Integer.parseInt(caseSer),
//				Integer.parseInt(caseYear));

		return ResponseEntity.ok(jlst);

	}

	// @GetMapping(path =
	// "/ncnByCase/restricted/{funcId}/{casePrefix}/{caseType}/{caseSer}/{caseYear}")
	@GetMapping(path = "/ncnByCase/restricted/{casePrefix}/{caseType}/{caseSer}/{caseYear}")
	public ResponseEntity<List<Judgm>> getNcnsByCaseRestricted(
			// @PathVariable(name = "funcId") String funcId,
			@PathVariable(name = "casePrefix") String casePrefix, @PathVariable(name = "caseType") String caseType,
			@PathVariable(name = "caseSer", required = false) String caseSer,
			@PathVariable(name = "caseYear", required = false) String caseYear) throws EntityNotFoundException {

		String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
				RequestAttributes.SCOPE_REQUEST);
		String case_no = String.format("%1$s%2$s%3$s/%4$s", casePrefix, caseType, caseSer, caseYear);
		System.out.println("Entered case# : " + case_no);
		System.out.println(funcId);

		List<Judgm> judgms = judgmService.getJudgmByCasesRestricted(casePrefix, caseType, Integer.parseInt(caseSer),
				Integer.parseInt(caseYear), funcId);

		return ResponseEntity.ok(judgms);

	}

	// @GetMapping(path =
	// "/ncnByCase/{funcId}/{casePrefix}/{caseType}/{caseSer}/{caseYear}")
	@GetMapping(path = "/ncnByCase/{casePrefix}/{caseType}/{caseSer}/{caseYear}")
	public ResponseEntity<List<Judgm>> getNcnsByCase(
			// @PathVariable(name = "funcId") String funcId,
			@PathVariable(name = "casePrefix") String casePrefix, @PathVariable(name = "caseType") String caseType,
			@PathVariable(name = "caseSer", required = false) String caseSer,
			@PathVariable(name = "caseYear", required = false) String caseYear) throws EntityNotFoundException {

		String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
				RequestAttributes.SCOPE_REQUEST);
		String case_no = String.format("%1$s%2$s%3$s/%4$s", casePrefix, caseType, caseSer, caseYear);

		System.out.println("Entered case# : " + case_no);
		System.out.println(funcId);

		List<Judgm> judgms = judgmService.getJudgmByCases(casePrefix, caseType, Integer.parseInt(caseSer),
				Integer.parseInt(caseYear), funcId);

//		if (mapper.getTypeMap(VJudgmCase.class, VJudgmCaseDto.class)==null)
//		{
//			TypeMap<VJudgmCase, VJudgmCaseDto> propertyMapper = mapper.createTypeMap(VJudgmCase.class, VJudgmCaseDto.class);
//		    // add deep mapping to flatten source's Player object into a single field in destination
//		    propertyMapper.addMappings(
//		      mapper -> mapper.map(src -> src.getVJudgm().getNcn(), VJudgmCaseDto::setNcn)
//		    );
//		}

//		List<VJudgmCase> judgmCases = vJudgmCaseService.getCases(case_no);
//		
//		List<VJudgmCaseDto> ldto = Arrays.asList(mapper.map(judgmCases,VJudgmCaseDto[].class));
//		 
		return ResponseEntity.ok(judgms);

	}

	@GetMapping(path = "/ncnByNcn/apl/{ncnNo}")
	public ResponseEntity<Response> getNcnApl(@PathVariable(name = "ncnNo", required = false) String ncnNo,
			Principal principal) throws Exception {

		// year-court-serno
		String[] ncnNoParts = ncnNo.split("-", -1);
		String ncnYear = ncnNoParts[0];
		String ncnCrt = ncnNoParts[1];
		String ncnSer = ncnNoParts[2];

		String ncn_no = String.format("[%1$s] %2$s %3$s", ncnYear, ncnCrt, ncnSer);
		System.out.println("Entered ncn: " + ncn_no);

		List<JudgmAplFr> jlst = judgmService.getJudgmAplFrByNcn(ncnYear.equals("") ? null : Integer.parseInt(ncnYear),
				ncnCrt.equals("") ? null : ncnCrt, ncnSer.equals("") ? null : Integer.parseInt(ncnSer));
//		List<Judgm> jlst = judgmService.getJugdmsByNcn(
//				ncnYear.equals("")?null:Integer.parseInt(ncnYear)
//				, ncnCrt.equals("")?null:  ncnCrt
//				, ncnSer.equals("")?null:Integer.parseInt(ncnSer)
//						);

		JSONObject dataObj = new JSONObject();
		dataObj.put("judgms", jlst);

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message("").redirectUrl("")
				.status(OK).statusCode(OK.value()).build());

	}

	// @GetMapping(path = "/ncnByNcn/{funcId}/{ncnYear}/{ncnCrt}/{ncnSer}")
	@GetMapping(path = "/ncnByNcn/{ncnYear}/{ncnCrt}/{ncnSer}")
	public ResponseEntity<Response> getNcn(
			// @PathVariable(name = "funcId") String funcId,
			@PathVariable(name = "ncnYear") String ncnYear, @PathVariable(name = "ncnCrt") String ncnCrt,
			@PathVariable(name = "ncnSer") String ncnSer, Principal principal) throws Exception {

		String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
				RequestAttributes.SCOPE_REQUEST);
		String ncn_no = String.format("[%1$s] %2$s %3$s", ncnYear, ncnCrt, ncnSer);
		System.out.println("Entered ncn: " + ncn_no);
		System.out.println(funcId);

		// String adName = principal.getName().toUpperCase().replace("@JUD.HKSARG", "");

		Judgm j = judgmService.getjugdmByNcn(Integer.parseInt(ncnYear), ncnCrt, Integer.parseInt(ncnSer), funcId);

		// if it is restricted
		// ...

		// check if judgm is confirmed:
//		JudgmXfr judgmXfr = j.getJudgmXfrs().get(0);
//		if(judgmXfr.getConfirmDate() == null || judgmXfr.getConfirmDate().toString().length() == 0)
//		{
//			// redirect
//			//return new ModelAndView("redirect:/ncns/confirm");
//		}

		System.out.println(j);

		JSONObject dataObj = new JSONObject();
		String message = null;
		String redirectUrl = null;

		if (j == null) {
			message = "NCN not found - " + ncn_no;

		} else if (!funcId.equals("MOD002")) {

			if (j.getRestricted().equals("Y")) {
				message = "NCN is restricted - " + ncn_no;
				dataObj.put("judgm", "");
				return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message(message)
						.redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());
			} else {
				// Check if revoked or confirmed
				//if (j.getStatus() == "I" || j.getJudgmXfrs().get(0).getConfirmDate() != null)
				if (j.getStatus() == "I" || j.getJudgmXfrs().iterator().next().getConfirmDate() != null)

					redirectUrl = "gotoJudgm/" + j.getJudgmId();
			}
		}

//		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(of("judgm", (j == null ? "" : j)))
//				.message(message).redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());

		dataObj.put("judgm", j == null ? "" : j);

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message(message)
				.redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());

//		return Optional.ofNullable(j)
//				        .map(ResponseEntity::ok)
//				        //.orElse(ResponseEntity.notFound().build());
//				        .orElseThrow(() -> new Exception("Student not found - " + ncn_no));

		// return ResponseEntity.

	}

	// @GetMapping(path =
	// "/ncnByNcn/restricted/{funcId}/{ncnYear}/{ncnCrt}/{ncnSer}")
	@GetMapping(path = "/ncnByNcn/restricted/{ncnYear}/{ncnCrt}/{ncnSer}")
	public ResponseEntity<Response> getNcnRetricted(
			// @PathVariable(name = "funcId") String funcId,
			@PathVariable(name = "ncnYear") String ncnYear, @PathVariable(name = "ncnCrt") String ncnCrt,
			@PathVariable(name = "ncnSer") String ncnSer, Principal principal
	// ,@RequestHeader Map<String, String> headers
	) throws Exception {

		String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
				RequestAttributes.SCOPE_REQUEST);

		String ncn_no = String.format("[%1$s] %2$s %3$s", ncnYear, ncnCrt, ncnSer);
		log.info("Entered ncn: " + ncn_no);

//		 headers.forEach((key, value) -> {
//			 log.info(String.format("Header '%s' = %s", key, value));
//		    });
//		 

//		String Restricted = "1";
//		Judgm j = judgmService.getjugdmByNcn(Integer.parseInt(ncnYear), ncnCrt, Integer.parseInt(ncnSer),Restricted);
		Judgm j = judgmService.getjugdmByNcn(Integer.parseInt(ncnYear), ncnCrt, Integer.parseInt(ncnSer), funcId);

		// if it is restricted
		// ...

		JSONObject dataObj = new JSONObject();
		String message = null;
		String redirectUrl = null;

		if (j == null) {
			message = "NCN not found - " + ncn_no;
		} else {
			if (j.getRestricted().equals("N")) {
				message = "NCN is public - " + ncn_no;
				dataObj.put("judgm", "");
				return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message(message)
						.redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());
			} else {
				// Check if revoked or confirmed
				//if (j.getStatus() == "I" || j.getJudgmXfrs().get(0).getConfirmDate() != null)
				if (j.getStatus() == "I" || j.getJudgmXfrs().iterator().next().getConfirmDate() != null)

					redirectUrl = "gotoJudgm/" + j.getJudgmId();
			}
		}

//		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(of("judgm", (j == null ? "" : j)))
//				.message(message).redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());

		dataObj.put("judgm", j == null ? "" : j);

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message(message)
				.redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());

	}

	@PostMapping(path = { "/update", "/update/confirm" }, consumes = { "multipart/form-data" })
	public Judgm updateJudgm(@RequestPart(value = "files", required = false) MultipartFile[] files,
			@RequestPart(value = "test_json", required = false) String judgmJson,
			@RequestPart(value = "titleUpdAddJson", required = false) String titlesJson,
			@RequestPart(value = "casesDelJson", required = false) String casesDelJson,
			@RequestPart(value = "titlesDelJson", required = false) String titlesDelJson,
			@RequestPart(value = "jjosDelJson", required = false) String jjosDelJson,
			@RequestPart(value = "appealsDelJson", required = false) String appealsDelJson,
			@RequestPart(value = "filesDelJson", required = false) String filesDelJson,
			@RequestPart(value = "ncnUrgent", required = false) String ncnUrgent, HttpServletRequest request

	) throws Exception {

		ObjectMapper o_mapper = new ObjectMapper();
		o_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Judgm judgm = o_mapper.readValue(judgmJson, Judgm.class);

		System.out.println("Entered in Judgm: " + judgmJson);
		System.out.println(request.getRequestURI());

		boolean toConfirm = request.getRequestURI().contains("/confirm") ? true : false;

		List<Long> listCaseDel = new ArrayList<Long>();
		List<Long> listTitleDel = new ArrayList<Long>();
		List<Long> listJjoDel = new ArrayList<Long>();
		List<Long> listAppealDel = new ArrayList<Long>();
		List<Long> listFileDel = new ArrayList<Long>();
		List<JudgmCaseTitle> listTitleUpdAdd = new ArrayList<JudgmCaseTitle>();
		// Judgm judgm0 = judgmRepository.getById(judgm.getJudgmId());

		try {
			listCaseDel = Arrays.asList(o_mapper.readValue(casesDelJson.toString(), Long[].class));
			listTitleDel = Arrays.asList(o_mapper.readValue(titlesDelJson.toString(), Long[].class));
			listJjoDel = Arrays.asList(o_mapper.readValue(jjosDelJson.toString(), Long[].class));
			listAppealDel = Arrays.asList(o_mapper.readValue(appealsDelJson.toString(), Long[].class));
			listFileDel = Arrays.asList(o_mapper.readValue(filesDelJson.toString(), Long[].class));
			listTitleUpdAdd = Arrays.asList(o_mapper.readValue(titlesJson.toString(), JudgmCaseTitle[].class));
		} catch (Exception ex) {
			System.out.println(ex);
		}

		// Validation
		// ......

		// Get User
		// NcnUser ncnUser = ncnUserService.findByNameWithRoles("TEST");
		NcnUser ncnUser = ncnUserService.findByNameWithRoles();

		// check case no against User Owner
		String badCaseNot0 = ncnUserService.checkCaseAgainstUserOwner(ncnUser, judgm, "MOD000", judgm.getNcnCrt());
		if (badCaseNot0 != null) {
			throw new CasePrefixNotAuthorizedException(badCaseNot0);
		}

		// check case exists
		String badCaseNot1 = ncnService.checkCaseExists(judgm);
		System.out.println(badCaseNot1);
		if (badCaseNot1!=null)
		{
			throw new CaseNotFoundException(badCaseNot1);
		}

		for (Long td : listTitleDel) {
			System.out.println("Delete Title ID: " + td);
		}

		judgm = ncnService.updateJudgm(judgm, listTitleUpdAdd, listCaseDel, listTitleDel, listJjoDel, listFileDel,
				listAppealDel, files, ncnUrgent, toConfirm);

		if (toConfirm) {
			judgm = judgmService.confirmJudgm((int) judgm.getJudgmId());

			return judgm;
//			JSONObject dataObj=new JSONObject();    
//			dataObj.put("judgm",(j == null ? "" : j));    
//			
//			return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).status(OK)
//					.statusCode(OK.value()).build());
		}

		return judgm;

	}

	@GetMapping(path = "/test/{caseid}")
	public void cascadeDelCase(@PathVariable(name = "caseid") Long caseid) throws Exception {
		{

			ncnService.cascadeDelCase(caseid);
		}
	}

	@PostMapping(path = "/create/restricted", consumes = { "multipart/form-data" })
	public Judgm createJudgmRestriced(@RequestPart(value = "test_json") String judgmJson) throws Exception {

		log.info("Entered in Judgm: " + judgmJson);

		ObjectMapper o_mapper = new ObjectMapper();
		o_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Judgm judgm = o_mapper.readValue(judgmJson, Judgm.class);

		NcnUser ncnUser = ncnUserService.findByNameWithRoles();

		// check case no against User Owner
		String badCaseNot0 = ncnUserService.checkCaseAgainstUserOwner(ncnUser, judgm, "MOD003", judgm.getNcnCrt());
		if (badCaseNot0 != null) {
			throw new CasePrefixNotAuthorizedException(badCaseNot0);
		}
		// check case exists 
		String badCaseNot1 = ncnService.checkCaseExists(judgm);
		System.out.println(badCaseNot1);
		if (badCaseNot1 != null) {
			throw new CaseNotFoundException(badCaseNot1);
		}

		judgm.setRestricted("Y");
		judgm = ncnService.createJudgmRestricted(judgm);

		log.info("Created Judgm: " + judgm.toString());

		return judgm;

	};

	@PostMapping(path = "/create", consumes = { "multipart/form-data" })
	// @ResponseStatus(HttpStatus.CREATED)
	public Judgm createJudgm(@RequestPart(value = "test_json", required = false) String judgmJson,
			@RequestPart(value = "files", required = false) MultipartFile[] files) throws Exception {

		log.info("Entered in Judgm: " + judgmJson);

		ObjectMapper o_mapper = new ObjectMapper();
		o_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Judgm judgm = o_mapper.readValue(judgmJson, Judgm.class);

		// Get User
		NcnUser ncnUser = ncnUserService.findByNameWithRoles();

		// check case no against User Owner
		String badCaseNot0 = ncnUserService.checkCaseAgainstUserOwner(ncnUser, judgm, "MOD000", judgm.getNcnCrt());
		if (badCaseNot0 != null) {
			throw new CasePrefixNotAuthorizedException(badCaseNot0);
		}
		// check case exists 
		String badCaseNot1 = ncnService.checkCaseExists(judgm);
		System.out.println(badCaseNot1);
		if (badCaseNot1!=null)
		{
			throw new CaseNotFoundException(badCaseNot1);		
		}

		judgm = ncnService.createJudgm(judgm, files);

		log.info("Created Judgm: " + judgm.toString());

		return judgm;

	};

	@GetMapping(path = "/last/{year}/{court}")
	public NcnLast getLastNcn(@PathVariable(name = "year") Integer year, @PathVariable(name = "court") String ncncrt)
			throws EntityNotFoundException {
		{
			return ncnLastService.getNcnLast(year, ncncrt);

		}
	}

	@PutMapping(path = "/last/increment", consumes = { "application/json" })
	public ResponseEntity<Response> incrementNcnLast(@RequestBody String data) throws Exception {

		{
			ObjectMapper o_mapper = new ObjectMapper();

			JsonNode node = o_mapper.readTree(data);

			Integer year = Integer.parseInt(node.get("year").textValue());
			String ncncrt = node.get("court").textValue();

			NcnLast t = ncnLastService.incrementNcnLast(year, ncncrt);

			return ResponseEntity.ok(Response.builder().timeStamp(now()).data(of("incrementNcnLast", t))
					.message("incrementNcnLast").status(OK).statusCode(OK.value()).build());

		}
	}

	@GetMapping(path = "/ncn/last/2/{year}/{court}")
	public ResponseEntity<Response> getServers(@PathVariable(name = "year") Integer year,
			@PathVariable(name = "court") String ncncrt) throws Exception {

		TimeUnit.SECONDS.sleep(3);
		return ResponseEntity
				.ok(Response.builder().timeStamp(now()).data(of("ncnLast", ncnLastService.getNcnLast(year, ncncrt)))
						.message("ncnLast retrieved").status(OK).statusCode(OK.value()).build());
	}

}