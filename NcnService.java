package hksarg.jud.ncns.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import hksarg.jud.ncns.model.FileBinaryData;
import hksarg.jud.ncns.model.Judgm;
import hksarg.jud.ncns.model.JudgmApl;
import hksarg.jud.ncns.model.JudgmCase;
import hksarg.jud.ncns.model.JudgmCaseTitle;
import hksarg.jud.ncns.model.JudgmFile;
import hksarg.jud.ncns.model.JudgmJjo;
import hksarg.jud.ncns.model.JudgmXfr;
//import hksarg.jud.ncns.model.Ncn;
import hksarg.jud.ncns.model.NcnCasePrefix;
import hksarg.jud.ncns.model.NcnLast;
import hksarg.jud.ncns.model.NcnUser;
import hksarg.jud.ncns.model.dataInterface.CaseNo;
import hksarg.jud.ncns.model.dataInterface.UserPermissions;
import hksarg.jud.ncns.model.dto.TitleDel;
//import hksarg.jud.ncns.model.TNcnLast;
//import hksarg.jud.ncns.model.VJudgm;
//import hksarg.jud.ncns.model.VJudgmCase;
//import hksarg.jud.ncns.model.VJudgmCaseTitle;
//import hksarg.jud.ncns.model.VJudgmFile;
//import hksarg.jud.ncns.model.VJudgmJjo;
import hksarg.jud.ncns.respository.NcnRepository;
import hksarg.jud.ncns.respository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import hksarg.jud.ncns.respository.FileBinaryDataRepository;
import hksarg.jud.ncns.respository.JudgmAplFrRespository;
import hksarg.jud.ncns.respository.JudgmAplRespository;
import hksarg.jud.ncns.respository.JudgmCaseRepository;
import hksarg.jud.ncns.respository.JudgmCaseTitleRepository;
import hksarg.jud.ncns.respository.JudgmFileRepository;
import hksarg.jud.ncns.respository.JudgmJjoRepository;
import hksarg.jud.ncns.respository.JudgmRepository;

@Service
@Transactional
@Slf4j
public class NcnService {

	@Autowired
	private Environment env;

	@Value("${app.globalUsername}")
	private String globalUserName;

	@Autowired
	NcnRepository ncnRepository;
	@Autowired
	JudgmRepository judgmRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	JudgmCaseRepository judgmCaseRepository;
	@Autowired
	JudgmCaseTitleRepository judgmCaseTitleRepository;
	@Autowired
	JudgmFileRepository judgmFileRepository;
	@Autowired
	FileBinaryDataRepository fileBinaryDataRepository;
	@Autowired
	JudgmJjoRepository judgmJjoRepository;
	@Autowired
	JudgmAplRespository judgmAplRespository;
	@Autowired
	JudgmAplFrRespository judgmAplFrRespository;

	@Autowired
	private JudgmService judgmService;
	@Autowired
	private JudgmCaseService judgmCaseService;
	@Autowired
	private JudgmCaseTitleService judgmCaseTitleService;
	@Autowired
	private JudgmFileService judgmFileService;
	@Autowired
	private NcnLastService ncnLastService;
//	@Autowired
//	private NcnCasePrefixService ncnCasePrefixService;
	@Autowired
	private NcnUserService ncnUserService;

	static Logger log = Logger.getLogger(NcnService.class);

	public String getCurrentlUser() {
		String currentUserName = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			currentUserName = authentication.getName();

		}
		if (currentUserName.isEmpty())
			currentUserName = globalUserName;

		return currentUserName;
	}

	public List<CaseNo> searchCaseNo(String part_caseno) {
		return ncnRepository.searchCaseNo(part_caseno);

	}
//
//	public Ncn getNcn(String ncn_no) {
//
//		// int ncn_id = 30822;
//		// Ncn ncn = ncnRepository.findOne((long) ncn_id);
//		// List<Ncn> ncn_list = ncnRepository.findByNcn(ncn_no);
//		Ncn ncn1 = ncnRepository.findByNcnIs(ncn_no);
//		// List<Ncn> ncn_list = ncnRepository.findByNcnQuery(ncn_no);
//		return ncn1;
//
//	}

	public void cascadeDelCase(Long caseId) {

		judgmCaseRepository.deleteById(caseId);
	}

	public List<Integer> getNcnYears() {
		List<Integer> ncnYears = new ArrayList<>();

		LocalDate today = LocalDate.now();
		Integer ncnYear = Integer.parseInt(env.getProperty("app.ncnLeastYear"));

		for (; ncnYear <= LocalDate.now().getYear(); ncnYear++) {
			ncnYears.add(ncnYear);
		}

		LocalDate dateNextYearNcn = LocalDate.of(LocalDate.now().getYear(),
				Integer.parseInt(env.getProperty("app.ncnNextYearMonth")), 1);
		if (today.isAfter(dateNextYearNcn)) {
			ncnYears.add((LocalDate.now().getYear() + 1));
		}
		Collections.sort(ncnYears, Collections.reverseOrder());

		return ncnYears;
	}

	public String checkCaseExists(Judgm judgm) {
		System.out.println("checkCasesExist");

		String badCaseNo = null;

		if (judgm.getJudgmCases() == null)
			return null;

		for (JudgmCase case0 : judgm.getJudgmCases()) {

			badCaseNo = null;
			CaseNo caseno = ncnRepository.findCaseNo(case0.getCaseCourtSys(), case0.getCaseType(), case0.getCaseSerNo(),
					case0.getCaseYr());

			String caseno_txt = "";
			System.out.println("For case0: " + case0.getCaseCourtSys() + case0.getCaseType()
					+ case0.getCaseSerNo().toString() + "/" + case0.getCaseYr().toString());
			if (caseno == null) {
				badCaseNo = case0.getCaseCourtSys() + case0.getCaseType() + case0.getCaseSerNo().toString() + "/"
						+ case0.getCaseYr().toString();

				System.out.println("Case no exist:" + badCaseNo);

				return badCaseNo;
			}

		}
		return null;

	}

	// @Transactional(propagation = Propagation.MANDATORY)
	public Judgm updateJudgm(Judgm judgm, List<JudgmCaseTitle> titlesUpdAdd, List<Long> casesDel, List<Long> titlesDel,
			List<Long> jjosDel, List<Long> filesDel, List<Long> appealsDel, MultipartFile[] filesUpload,
			String ncnUrgent, boolean toConfirm) throws Exception {

		String usrName = getCurrentlUser();

		// userRepository.setUserContextT(usrName, "MOD000", "/ncns/updateJudgm");

		LocalDateTime date = LocalDateTime.now();

		// get the existing
//		Judgm judgm1 = null;
		Judgm judgm1 = judgmService.getJudgm(judgm.getJudgmId());
		log.info(judgm1.toString());
		if (!toConfirm) {
//			judgm1.setLastOverallUpdDate(date);
//			judgm1.setLastOverallUpdBy(usrName);
		}
		judgm1.setDocType(judgm.getDocType());
		judgm1.setHandDownDate(judgm.getHandDownDate());
		judgm1.setHrngOrPaper(judgm.getHrngOrPaper());
		judgm1.setJudgmApls(judgm.getJudgmApls());

		if (ncnUrgent != null)
			// judgm1.getJudgmXfrs().get(0).setUrgent(ncnUrgent);;
			judgm1.getJudgmXfrs().iterator().next().setUrgent(ncnUrgent);

		String ncn_court = judgm1.getNcnCrt();
		// judgm1.setJudgmJjos(judgm.getJudgmJjos());
		// judgm1.setStatus("A");

		log.info(judgm.toString());

		// System.out.println(judgm1.getJudgmApls().size());

		if (judgm1.getJudgmApls() != null) {

			for (JudgmApl apl : judgm1.getJudgmApls()) {
				// System.out.println(apl.getNcnCrt() + " " + apl.getJudgmId() + " " +
				// apl.getJudgmAplFrJudgmId());
				System.out.println(apl.getJudgmId() + " " + apl.getJudgmAplFrJudgmId());

			}
			judgmAplRespository.saveAll(judgm1.getJudgmApls());
		}

//		for (Long caseId : casesDel)
//		{
//			JudgmCase jc = judgmCaseRepository.getOne(caseId);
//			
//			
//			judgm1.getJudgmCases().remove(jc);
//		}
		judgm1 = judgmRepository.saveAndFlush(judgm1);

		// Delete Title
		// if (titlesDel.size()>0)
		// judgmCaseTitleRepository.deleteByIdIn(titlesDel);
		// judgmCaseTitleRepository.deleteAllById(titlesDel);
		// judgmCaseTitleRepository.flush();

		// Delete Case Titles will also be deleted
		// ......

		// Delete Case
		if (casesDel.size() > 0)
			judgmCaseRepository.deleteAllById(casesDel);
		// judgmCaseRepository.deleteByIdIn(casesDel);

		// Delete Title
		log.info("Delete Title" + titlesDel.size());
		if (titlesDel.size() > 0)
			// judgmCaseTitleRepository.deleteAllById(titlesDel);
			judgmCaseTitleRepository.deleteByIdIn(titlesDel);
		// Delete Jjo
		System.out.println("jjosDel :  " + jjosDel.size());
		if (jjosDel.size() > 0) {
			// judgmJjoRepository.deleteAllById(jjosDel);
			judgmJjoRepository.deleteByIdIn(jjosDel);
		}
		if (appealsDel.size() > 0)
			judgmAplRespository.deleteAllById(appealsDel);
		// judgmJjoRepository.deleteByIdIn(jjosDel);
		// Delete file
		System.out.println("File: Delete " + filesDel.size());
		if (filesDel.size() > 0) {
			for (Long id : filesDel) {
				JudgmFile f = judgmFileRepository.getOne(id);
				// List<JudgmCaseTitle> list_jct = f.getJudgmCaseTitles();
				Set<JudgmCaseTitle> list_jct = f.getJudgmCaseTitles();
				judgmCaseTitleRepository.deleteInBatch(list_jct);
//				for (JudgmCaseTitle jct : list_jct )
//				{
//					System.out.print("Delete jct" + jct.toString());
//					
//				}
			}
			judgmFileRepository.deleteAllById(filesDel);
		}
		// judgmFileRepository.deleteByIdIn(filesDel);

		Set<JudgmCase> cases = judgm.getJudgmCases();
		// 20230327 Start
		// Set<JudgmFile> files = judgm.getJudgmFiles();
		Set<JudgmFile> files = null;
		Set<JudgmXfr> judgmXfrs = judgm.getJudgmXfrs();

		if (judgmXfrs != null) {

			files = judgm.getJudgmXfrs().iterator().next().getJudgmFiles();
			System.out.println("judgmXfrs size " + judgmXfrs.size());
			System.out.println("File size " + files.size());
		}

		// 20230327 End
		Set<JudgmJjo> jjos = judgm.getJudgmJjos();

		// cases
		int i = 0;
		List<JudgmCase> cls = new ArrayList<JudgmCase>();
		if (cases != null) {
			for (JudgmCase case0 : cases) {

				// if (case0.getNcnCrt() == null)
				// case0.setNcnCrt(ncn_court); // temp

				// log.info("rowStatus >" + case0.rowStatus);
				// List<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();
				Set<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();

				for (JudgmCaseTitle title0 : titles) {
					// log.info(title0.toString());
					title0.setJudgmCase(case0);
					// title0.setJudgmXfr(judgm1.getJudgmXfrs().get(0)); // to be updated

					// if (title0.getNcnCrt() == null)
					// title0.setNcnCrt(ncn_court); // temp
				}

				case0.setJudgm(judgm1);

				// cases.set(i, case0);

				// judgmCaseService.create(case0);

			}

			cls = judgmCaseRepository.saveAllAndFlush(cases);

		}

		// Titles
		for (JudgmCaseTitle title0 : titlesUpdAdd) {
			System.out.println(title0.getJudgmCaseTitleId());
			System.out.println(title0.getJudgmCaseId());
			System.out.println(title0.getJudgmFileId());
			// if (title.getJudgmCaseTitleId()=="")

			System.out.println(title0.toString());
			if (title0.getJudgmCaseTitleId() == 0) // new title
			{
				System.out.println("New Title");

//		    	if (title0.getJudgmCaseId()==null) 
//		    	{
				Set<JudgmCase> cases_ = judgm1.getJudgmCases();
				cases_.addAll(cls);
				for (JudgmCase case0 : cases_) {
					String caseno = case0.getCaseCourtSys() + case0.getCaseType() + case0.getCaseSerNo() + "/"
							+ case0.getCaseYr();

					System.out.println("caseno: " + caseno);
					if (caseno.equals(title0.getCaseno())) {
						System.out.println("MAP: " + caseno);
						title0.setJudgmCase(case0);
					}
//						
				}
//                }

				JudgmFile file0 = judgmFileRepository.getById(title0.getJudgmFileId());
				title0.setJudgmFile(file0);
				// title0.setNcnCrt(ncn_court); // temp
				System.out.println(title0.toString());
				judgmCaseTitleService.create(title0);
			} else

			{

				// title0.setNcnCrt(ncn_court);

				// title0.setJudgmXfr(judgm1.getJudgmXfrs().get(0)); //to be updated

				JudgmCase case0 = judgmCaseRepository.getById(title0.getJudgmCaseId());
				JudgmFile file0 = judgmFileRepository.getById(title0.getJudgmFileId());
				title0.setJudgmCase(case0);
				title0.setJudgmFile(file0);

				judgmCaseTitleService.create(title0);
			}
		}

		// jjos
		if (jjos != null) {
			for (JudgmJjo jjo : jjos) {
				jjo.setJudgm(judgm1);
				// jjo.setNcnCrt(ncn_court); // temp
			}
			judgmJjoRepository.saveAll(jjos);
		}
		// files (only handle the new file
//
//		if (1==1)
//			throw new RuntimeException();

		if (files != null) {
			int fileCnt = 0;
			for (JudgmFile file0 : files) {
				System.out.println("file0*: " + file0.getFileName());

				// file0.setJudgm(judgm1); //20230327
				// byte[] fileData = new byte[10];
				log.debug("JudgmFileId : " + file0.getJudgmFileId());

				// Update new file only
				// if (Long.toString(file0.getJudgmFileId())=="")
				// {
				if (filesUpload != null) {
					byte[] fileData = filesUpload[fileCnt].getBytes();
					// file0.setFileData(fileData); 20230412
					// file0.setFileName(file0.getFileName() + (new Random().nextInt(100) + 5));
					file0.setFileName(file0.getFileName());
				}
//				byte[] fileData = null;
//				fileData = filesUpload[fileCnt].getBytes();

				// }
				// file0.setNcnCrt(ncn_court); // temp
				// file0.setJudgmXfr(judgm1.getJudgmXfrs().get(0));
				file0.setJudgmXfr(judgm1.getJudgmXfrs().iterator().next());

				// 20230424
				// fileCnt++;

				if (file0.getFileType().equals("J")) {

					if (file0.getJudgmCaseTitles() != null) {
						for (JudgmCaseTitle title0 : file0.getJudgmCaseTitles()) {

							System.out.println("title0*: " + title0.toString());
							if (title0.getJudgmCaseTitleId() > 0) {
								System.out.println("skip");
								continue;
							}
							System.out.println("go on");
							Set<JudgmCase> cases_ = judgm1.getJudgmCases();
							cases_.addAll(cls);
							for (JudgmCase case0 : cases_) {
								String caseno = case0.getCaseCourtSys() + case0.getCaseType() + case0.getCaseSerNo()
										+ "/" + case0.getCaseYr();

								System.out.println("caseno*: " + caseno);

								if (caseno.equals(title0.getCaseno())) {
									System.out.println("Map caseno: " + caseno);
									System.out.println(case0.toString());
									title0.setJudgmCase(case0);
									title0.setJudgmCaseId(case0.getJudgmCaseId());

								}
							}

							title0.setJudgmFile(file0);
							// title0.setNcnCrt(ncn_court); // temp
							System.out.println(ncn_court);
							System.out.println(title0.toString());

						}

					}
				}

				// 20230424 START
				JudgmFile jf = judgmFileRepository.saveAndFlush(file0);

				FileBinaryData fdata = fileBinaryDataRepository.getById(jf.getJudgmFileId());

				byte[] fileData = filesUpload[fileCnt].getBytes();

				fdata.setFileData(fileData);

				fileBinaryDataRepository.save(fdata);

				fileCnt++;
				// 20230424 END
			}
			// 20230424
			// judgmFileRepository.saveAll(files);

		}

		log.info("updateJudgm Done");

		return judgm1;
		// return judgm1 = judgmRepository.saveAndFlush(judgm);
	}

	// @Transactional(rollbackOn = SQLException.class)
	@Transactional
	public Judgm createJudgmRestricted(Judgm judgm) throws Exception {
		String usrName = getCurrentlUser();

		judgm.setCreateBy(usrName);

		// userRepository.setUserContextT(usrName, "MOD003", "/ncns/create/restricted");

		NcnLast ncnLast = null;
		String newNcn = "";
		String ncn_court = judgm.getNcnCrt();
		Integer ncn_year = judgm.getNcnYr();

		ncnLast = ncnLastService.getNcnLast(judgm.getNcnYr(), judgm.getNcnCrt());
		newNcn = "[" + ncn_year.toString() + "] " + ncn_court + " " + (ncnLast.getLastNo() + 1);
		System.out.println("new NCN: " + newNcn);
		judgm.setNcnNo(ncnLast.getLastNo() + 1);

		ncnLastService.incrementNcnLast(ncn_year, ncn_court);
		LocalDateTime createDt = LocalDateTime.now();

		JudgmXfr judgmXfr = new JudgmXfr();
		judgmXfr.setXfrNo(1);
		// judgmXfr.setNcnCrt(court); //temp
		// judgm.setJudgmXfrs(new ArrayList<JudgmXfr>());
		judgm.setJudgmXfrs(new HashSet<JudgmXfr>());
		judgm.addJudgmXfr(judgmXfr);
		judgm.setCreateDate(createDt);
		judgm.setCreateBy(usrName);
//		judgm.setLastOverallUpdDate(createDt);
//		judgm.setLastOverallUpdBy(usrName);
		judgm.setStatus("A");

		Set<JudgmCase> cases = judgm.getJudgmCases();

		judgm = judgmRepository.saveAndFlush(judgm);

		log.info("JudgmXfr ID" + judgmXfr.getJudgmXfrId());
		// cases
		for (JudgmCase case0 : cases) {
			// List<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();
			Set<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();
			case0.setJudgm(judgm);
			// case0.setNcnCrt(ncn_court); // temp
			log.info("case0 " + case0.getJudgmCaseId() + "| ");

			judgmCaseService.create(case0);
		}

		log.info("Create Restricted NCN");

		return judgmRepository.getById(judgm.getJudgmId());

	}

//	@Transactional(rollbackOn = SQLException.class)
	@Transactional
//	public VJudgm createNcn (
//			VJudgm vJudgm, 
//			List<VJudgmCase> vCases, 
//			ArrayList<ArrayList<VJudgmCaseTitle>> vCaseTitles,
//			List<VJudgmFile> vFiles,
//			List<VJudgmJjo> vJjos) throws Exception

	public Judgm createJudgm(Judgm judgm, MultipartFile[] filesUpload) throws Exception {

		// Maintain Judgment
		String usrName = getCurrentlUser();

		judgm.setCreateBy(usrName);

		// userRepository.setUserContextT(usrName, "MOD000", "/ncns/create");

		// Parse for Court and Year
//		//String pattern_ncn = "\\[(2018|2019|20[2-9][0-9])\\]([\\s|&nbsp;]*)(HKCFA|HKCA|HKCFI|HKCT|HKDC|HKFC|HKLdT|HKMagC|HKCrC|HKLaT|HKSCT|HKOAT)([\\s|&nbsp;]*)(\\d+)";
//		String pattern_ncn = "\\[(19[0-9][0-9]|20[0-9][0-9])\\]([\\s|&nbsp;]*)(HKCFA|HKCA|HKCFI|HKCT|HKDC|HKFC|HKLdT|HKMagC|HKCrC|HKLaT|HKSCT|HKOAT)([\\s|&nbsp;]*)(\\d*)";
//		Pattern NCN_PATTERN = Pattern.compile(pattern_ncn);
//		//Matcher mp = NCN_PATTERN.matcher(judgm.getNcn());
//		Matcher mp = NCN_PATTERN.matcher("["+judgm.getNcnYr().toString()+"] "+ judgm.getNcnCrt()  );
//		
//		String court = "";
//		String year = "";
//		Boolean validYrAndCourt = false;
//		while (mp.find())
//		{
//			validYrAndCourt = true;
//			year = mp.group(1);
//			court = mp.group(3);
//			break;
//		}

		NcnLast ncnLast = null;
		String newNcn = "";
		String ncn_court = judgm.getNcnCrt();
		Integer ncn_year = judgm.getNcnYr();

//		if (validYrAndCourt)
//		{
		ncnLast = ncnLastService.getNcnLast(judgm.getNcnYr(), judgm.getNcnCrt());
		newNcn = "[" + ncn_year.toString() + "] " + ncn_court + " " + (ncnLast.getLastNo() + 1);
		System.out.println("new NCN: " + newNcn);

		// judgm.setNcnYr(Integer.parseInt(year));
		judgm.setNcnNo(ncnLast.getLastNo() + 1);

//			/judgm.setNcn(newNcn);

//		}

		// if (ncnLast.getLastNo()>0)
		ncnLastService.incrementNcnLast(ncn_year, ncn_court);
		LocalDateTime createDt = LocalDateTime.now();

		JudgmXfr judgmXfr = judgm.getJudgmXfrs().iterator().next();
		judgmXfr.setXfrNo(1);
		judgmXfr.setJudgm(judgm);

//		JudgmXfr judgmXfr = new JudgmXfr();
//		judgmXfr.setXfrNo(1);
//		judgm.setJudgmXfrs(new HashSet<JudgmXfr>());

//		judgm.addJudgmXfr(judgmXfr);
		judgm.setCreateDate(createDt);
		judgm.setCreateBy(usrName);
//		judgm.setLastOverallUpdDate(createDt);
//		judgm.setLastOverallUpdBy(usrName);

		judgm.setStatus("A");

		Set<JudgmCase> cases = judgm.getJudgmCases();
		// Set<JudgmFile> files = judgm.getJudgmFiles(); //20230327
		Set<JudgmFile> files = null;

		files = judgm.getJudgmXfrs().iterator().next().getJudgmFiles();
		System.out.println("judgmXfrs size " + judgm.getJudgmXfrs());
		System.out.println("File size " + (files != null ? files.size() : "0"));

		Set<JudgmJjo> jjos = judgm.getJudgmJjos();
		Set<JudgmApl> apls = judgm.getJudgmApls();

//		for (JudgmFile file0 : files)
//		{
//			for (JudgmCaitle title0 : file0.getJudgmCaseTitles())
//			{
//				System.out.println(title0);
//			}
//		}
		// jjo
		for (JudgmJjo jjo0 : jjos) {
			jjo0.setJudgm(judgm);
			// jjo0.setNcnCrt(ncn_court); // temp
		}

		// SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		// System.out.println(formatter.format(createDt));

		judgm = judgmRepository.saveAndFlush(judgm);

		// Apl
		if (apls != null) {
			for (JudgmApl apl : apls) {
				apl.setJudgmId(judgm.getJudgmId());
				// apl.setNcnCrt(ncn_court); // temp
			}
			judgmAplRespository.saveAll(judgm.getJudgmApls());
		}
		log.info("JudgmXfr ID" + judgmXfr.getJudgmXfrId());
		// cases
		for (JudgmCase case0 : cases) {
			// List<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();
			Set<JudgmCaseTitle> titles = case0.getJudgmCaseTitles();
			case0.setJudgm(judgm);
			// case0.setNcnCrt(ncn_court); // temp
			log.info("case0 " + case0.getJudgmCaseId() + "| ");
			// cases.set(i, case0);

//			for (JudgmCaseTitle title0 : titles)
//			{
//				title0.setJudgmCase(case0);
//				//title0.setJudgmXfr(judgmXfr);    // to be updated
//				title0.setNcnCrt(ncn_court);  //temp
//				//title0 = judgmCaseTitleService.create(title0);
//			}
			judgmCaseService.create(case0);
		}
		// files

		int fileCnt = 0;
		if (files != null) { // 20230327
			for (JudgmFile file0 : files) {

				// file0.setJudgm(judgm); //20230327
				file0.setJudgmXfr(judgmXfr);

				if (filesUpload != null) {
					byte[] fileData = filesUpload[fileCnt].getBytes();
					// file0.setFileData(fileData); 20230412
					// file0.setFileName(file0.getFileName() + (new Random().nextInt(100) + 5));
					file0.setFileName(file0.getFileName());
				}
				// byte[] fileData = new byte[10];
				// file0.setFileData(fileData);

				log.info("File " + file0.getJudgmFileId() + "| " + file0.getFileName() + "| " + file0.getLang());
				// file0.setNcnCrt(ncn_court); // temp

				if (file0.getJudgmCaseTitles() != null) // 20230124
				{
					for (JudgmCaseTitle title0 : file0.getJudgmCaseTitles()) {
						for (JudgmCase case0 : cases) {
							String caseno = case0.getCaseCourtSys() + case0.getCaseType() + case0.getCaseSerNo() + "/"
									+ case0.getCaseYr();

							log.info("caseno" + caseno);
							if (caseno.equals(title0.getCaseno())) {

								title0.setJudgmCase(case0);

							}
						}

						title0.setJudgmFile(file0);
						// title0.setNcnCrt(ncn_court); // temp

					}

				}
				// 20230424
				// judgmFileService.create(file0);

				JudgmFile jf = judgmFileService.create(file0);

				FileBinaryData fdata = fileBinaryDataRepository.getById(jf.getJudgmFileId());

				byte[] fileData = filesUpload[fileCnt].getBytes();

				fdata.setFileData(fileData);

				fileBinaryDataRepository.save(fdata);

				// 20230424 END

				fileCnt++;
			}
		}

		// if (true)
		// throw new RuntimeException();

		log.info("Create NCN");

		return judgmRepository.getById(judgm.getJudgmId());

	}

}