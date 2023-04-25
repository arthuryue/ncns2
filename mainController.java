package hksarg.jud.ncns.controller;


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

    private final Logger log = LoggerFactory.getLogger(MaintenanceController.class);


    

    @GetMapping("/thymeleaf/judgmForm")
    public ModelAndView showUpdateForm(@RequestParam Long id, Model model) {
   	 ModelAndView mav = new ModelAndView("update-judgm-form");
   	 Judgm j = judgmRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
   	 mav.addObject("judgm", j);
   	 return mav;
    }

    @GetMapping(value = { "/main/restricted" })
    public ModelAndView maintenanceRestricted(Principal principal) {

   	 // Get User
   	 String userName = SecurityContextHolder.getContext().getAuthentication().getName();

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



   	 return mav;
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

   	 log.info("[judgmId] :" + String.valueOf(judgmId));

   	 Judgm j = judgmService.revokeJudgm(judgmId, userName);

   	 System.out.println(j);

   	 // return ResponseEntity.ok(j);
   	 JSONObject dataObj = new JSONObject();
   	 dataObj.put("judgm", (j == null ? "" : j));

   	 return ResponseEntity
   			 .ok(Response.builder().timeStamp(now()).data(dataObj).status(OK).statusCode(OK.value()).build());

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


   	 JSONObject dataObj = new JSONObject();
   	 dataObj.put("judgms", jlst);

   	 return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message("").redirectUrl("")
   			 .status(OK).statusCode(OK.value()).build());

    }


    @GetMapping(path = "/ncnByNcn/{ncnYear}/{ncnCrt}/{ncnSer}")
    public ResponseEntity<Response> getNcn(
   		 @PathVariable(name = "ncnYear") String ncnYear, @PathVariable(name = "ncnCrt") String ncnCrt,
   		 @PathVariable(name = "ncnSer") String ncnSer, Principal principal) throws Exception {

   	 String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
   			 RequestAttributes.SCOPE_REQUEST);
   	 String ncn_no = String.format("[%1$s] %2$s %3$s", ncnYear, ncnCrt, ncnSer);
   	 System.out.println("Entered ncn: " + ncn_no);
   	 System.out.println(funcId);


   	 Judgm j = judgmService.getjugdmByNcn(Integer.parseInt(ncnYear), ncnCrt, Integer.parseInt(ncnSer), funcId);



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



   	 dataObj.put("judgm", j == null ? "" : j);

   	 return ResponseEntity.ok(Response.builder().timeStamp(now()).data(dataObj).message(message)
   			 .redirectUrl(redirectUrl).status(OK).statusCode(OK.value()).build());



    }

    @GetMapping(path = "/ncnByNcn/restricted/{ncnYear}/{ncnCrt}/{ncnSer}")
    public ResponseEntity<Response> getNcnRetricted(
   		 @PathVariable(name = "ncnYear") String ncnYear, @PathVariable(name = "ncnCrt") String ncnCrt,
   		 @PathVariable(name = "ncnSer") String ncnSer, Principal principal
    ) throws Exception {

   	 String funcId = (String) RequestContextHolder.getRequestAttributes().getAttribute("FUNC_ID",
   			 RequestAttributes.SCOPE_REQUEST);

   	 String ncn_no = String.format("[%1$s] %2$s %3$s", ncnYear, ncnCrt, ncnSer);
   	 log.info("Entered ncn: " + ncn_no);

   	 Judgm j = judgmService.getjugdmByNcn(Integer.parseInt(ncnYear), ncnCrt, Integer.parseInt(ncnSer), funcId);



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

   	 for (Long td : listTitleDel) {
   		 System.out.println("Delete Title ID: " + td);
   	 }

   	 judgm = ncnService.updateJudgm(judgm, listTitleUpdAdd, listCaseDel, listTitleDel, listJjoDel, listFileDel,
   			 listAppealDel, files, ncnUrgent, toConfirm);

   	 if (toConfirm) {
   		 judgm = judgmService.confirmJudgm((int) judgm.getJudgmId());

   		 return judgm;

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



}
