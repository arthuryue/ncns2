package hksarg.jud.ncns.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hksarg.jud.ncns.model.Judgm;
import hksarg.jud.ncns.model.JudgmAplFr;
import hksarg.jud.ncns.model.Response;
import hksarg.jud.ncns.model.dataInterface.CaseNo;
import hksarg.jud.ncns.model.dataInterface.UserMenu;
import hksarg.jud.ncns.model.dataInterface.UserPermissions;
import hksarg.jud.ncns.respository.JudgmRepository;
import hksarg.jud.ncns.security.NcnUserDetailsService;
import hksarg.jud.ncns.service.JudgmCaseService;
import hksarg.jud.ncns.service.JudgmService;
import hksarg.jud.ncns.service.NcnLastService;
import hksarg.jud.ncns.service.NcnService;
import hksarg.jud.ncns.service.NcnUserService;
import hksarg.jud.ncns.service.Impl.NcnServiceImpl;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/ncns")
public class EnquiryController {
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

	private final Logger log = LoggerFactory.getLogger(EnquiryController.class);

	@GetMapping(path = "/searchcaseno/{part_caseno}")
	public ResponseEntity<Response> searchCaseNo(@PathVariable(name = "part_caseno") String part_caseno)
	// (@PathVariable(name = "judgmId") Long judgmId)
	{
		List<CaseNo> c = ncnService.searchCaseNo(part_caseno);
		String message = null;

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(of("caseno", (c == null ? "" : c)))
				.message(message).status(OK).statusCode(OK.value()).build());

	}

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






	



	

}