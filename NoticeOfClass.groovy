/*
 ***************************************************************
 *                                                             *
 *                           NOTICE                            *
 *                                                             *
 *   THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             *
 *   CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   *
 *   OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  *
 *   WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       *
 *   ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  *
 *   THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            *
 *   ALL OTHER RIGHTS RESERVED.                                *
 *                                                             *
 *   (c) COPYRIGHT 2020 INFOR.  ALL RIGHTS RESERVED.           *
 *   THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            *
 *   TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          *
 *   AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        *
 *   RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         *
 *   THE PROPERTY OF THEIR RESPECTIVE OWNERS.                  *
 *                                                             *
 ***************************************************************
 */

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *ABF_R_105        20220405  RDRIESSEN   Mods BF0105- Write to extension file EXTCLS as a basis for a Notice of Classification Report
 *ABF_R_105        20220511  KVERCO      Update for XtendM3 review feedback
 *ABF_R_105        20220512  RDRIESSEN   additional lines added to the report 
 */

 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;

 /*
 * Populate table EXTCLS for Notice of Classification report
 */
  public class NoticeOfClass extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String divi;
  private String bref;
  private String supplier;
  private String suno;
  private String sudo;  
  private String sunm;
  private String itno;
  private String bins; 
  private String atnr;   
  private String unms;
  private String atid; 
  private String atvn;
  private double atvn2;
  
  private String atva;
  private String tx30; 
  private String tx15;
  private String tx31;
  private String name;
  private String cat1;
  private String ref1;
  private String ref2;
  private String ref3;
  private String ref4;
  private String itds;
  private List lstQITests_Range;
  private List lstQITests_Target;
  private List lstQITests_Quality;
  private List lstTestResults_Range;
  private List lstSortedTestResults_Range;
  private List lstTestResults_Target;
  private List lstSortedTestResults_Target;
  private List lstTestResults_Quality;
  private List lstSortedTestResults_Quality;
  private List lstTestResults_Range01;
  private List lstSortedTestResults_Range01;
  private List lstTestResults_Target01;
  private List lstSortedTestResults_Target01;
  private List lstTestResults_Quality01;
  private List lstSortedTestResults_Quality01;
  private int XXCONO;
 
  public NoticeOfClass(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
    cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
  	if (cono == "?") {
  	  cono = "";
  	} 
  	
  	divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
  	if (divi == "?") {
  	  divi = "";
  	} 
  	
  	itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
  	if (itno == "?") {
  	  itno = "";
  	}	
  	
  	bref = mi.inData.get("BREF") == null ? '' : mi.inData.get("BREF").trim();
  	if (bref == "?") {
  	  bref = "";
  	} 

  	//Validate input fields
    if (!cono.isEmpty()) {
		  if (cono.isInteger()){
			  XXCONO= cono.toInteger();
			} else {
				mi.error("Company " + cono + " is invalid");
				return;
		}
		} else {
			XXCONO= program.LDAZD.CONO;
		}
		
		if (divi.isEmpty()) {
		  program.LDAZD.DIVI;
		} else {
		  DBAction queryCMNDIV = database.table("CMNDIV").index("00").selection("CCDIVI").build();
      DBContainer CMNDIV = queryCMNDIV.getContainer();
      CMNDIV.set("CCCONO", XXCONO);
      CMNDIV.set("CCDIVI", divi);
      if(!queryCMNDIV.read(CMNDIV)) {
        mi.error("Division does not exist.");
        return;
      } 
		}
  	
  	// - validate itno
    DBAction queryMITMAS = database.table("MITMAS").index("00").selection("MMITNO").build();
    DBContainer MITMAS = queryMITMAS.getContainer();
    MITMAS.set("MMCONO", XXCONO);
    MITMAS.set("MMITNO", itno);
    if (!queryMITMAS.read(MITMAS)) {
      mi.error("Item no is invalid.");
      return;
    }
  	if (bref.isEmpty()) {
  	  mi.error("BREF must be entered.");
      return;
  	}
    deleteEXTCLS(cono, divi, itno, bref);
  	writeEXTCLS(cono, divi, itno, bref);
  	writeLOTREF(cono, divi, itno, bref);
  }
  
 
 /*
 * deleteEXTCLS - delete existing records with key cono-divi-itno-bref
 */
  def deleteEXTCLS(String cono, String divi, String itno, String bref) {
    
    DBAction queryEXTCLS = database.table("EXTCLS").index("00").selection("EXCONO", "EXDIVI", "EXITNO", "EXBREF").build();
    DBContainer EXTCLS = queryEXTCLS.getContainer();
    EXTCLS.set("EXCONO", XXCONO);
    EXTCLS.set("EXDIVI", divi); 
    EXTCLS.set("EXITNO", itno);
    EXTCLS.set("EXBREF", bref);
    queryEXTCLS.readAllLock(EXTCLS, 4, deleteEXTCLS);
  }
    
 /*
 * deleteEXTCLS - Callback function
 */
  Closure<?> deleteEXTCLS = { LockedResult EXTCLS ->
    EXTCLS.delete();
  }

 /*
 * write EXTCLS - write records to EXTCLS 
 */
  def writeEXTCLS(String cono, String divi, String itno, String bref) {
	
	  //Current date and time
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
  	def params01 = ["ITNO": itno.toString(),]; 
    
    def callback01 = {
      Map<String, String> response ->
      if(response.ITDS != null){
        itds =  response.ITDS; 
      }
    }
    
  	miCaller.call("MMS200MI","Get", params01, callback01);
  	
  	DBAction actionEXTCLS = database.table("EXTCLS").build();
  	DBContainer EXTCLS = actionEXTCLS.getContainer();
  
    // write to transaction file EXTCLS as main table for Notice of Classification Report
  	EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXCAT1", 'L');
  	EXTCLS.set("EXREF1", '.');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
  	actionEXTCLS.insert(EXTCLS, recordExists);
  	
  	// write to transaction file EXTCLS as main table for Notice of Classification Report
  	EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXCAT1", 'L');
  	EXTCLS.set("EXREF1", '.');
  	EXTCLS.set("EXREF2", '.');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
  	actionEXTCLS.insert(EXTCLS, recordExists);
  	
  	// write to transaction file EXTCLS as main table for Notice of Classification Report
  	EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXCAT1", 'M');
  	EXTCLS.set("EXREF2", 'Date:');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
  	actionEXTCLS.insert(EXTCLS, recordExists);
  	
  	// write to transaction file EXTCLS as main table for Notice of Classification Report
  	EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXCAT1", 'M');
  	EXTCLS.set("EXREF2", 'Signature:');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
  	actionEXTCLS.insert(EXTCLS, recordExists);
  	
	}
  
 
 /*
 * Write data to Lot Reference extension table, EXTCLS
 *
 */ 
  def writeLOTREF(String cono, String divi, String itno,  String bref) {
	  
	  int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    int currentCompany = (Integer)program.getLDAZD().CONO;
	  
	  lstTestResults_Range = new ArrayList();
  		
  	Map<String,String> headers = ["Accept": "application/json"];
    Map<String,String> params = [ "LMITNO":itno, "LMBREF":bref, "maxrecs": "500"];
    String url = "/M3/m3api-rest/v2/execute/CMS100MI/Lst_ZATS101C";
      
    int nobins = 0;
      
    IonResponse response = ion.get(url, headers, params);
    if (response.getStatusCode() == 200) {
      JsonSlurper jsonSlurper = new JsonSlurper();
      Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(response.getContent());
      ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
       
      ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
      recordList.eachWithIndex { it, index ->
        Map<String, String> recordQMSTRS = (Map<String, String>) it;
        if (lstTestResults_Range.size() < 500) {
          suno = recordQMSTRS.ORPAN1;
    	    sunm = recordQMSTRS.IDSUNM;
    	    sudo = recordQMSTRS.ORPAN2;
    	    
    	    if(recordQMSTRS.ORPANR != '') { nobins = nobins + 1;
    	    }
    	    
    	    bins = recordQMSTRS.ORPANR;
    	    atnr = recordQMSTRS.LMATNR;
    	  
          DBAction actionEXTCLS = database.table("EXTCLS").build();
  	      DBContainer EXTCLS = actionEXTCLS.getContainer();
        
          EXTCLS.set("EXCONO", XXCONO);
  	      EXTCLS.set("EXDIVI", divi);
  	      EXTCLS.set("EXBREF", bref);
  	      EXTCLS.set("EXITNO", itno);
  	      EXTCLS.set("EXCAT1", 'K');
  	      EXTCLS.set("EXREF1", '....');
  	      EXTCLS.set("EXREF2", bins);
  	      EXTCLS.set("EXREF3", '');
  	      EXTCLS.set("EXRGDT", currentDate);
  	      EXTCLS.set("EXRGTM", currentTime);
  	      EXTCLS.set("EXLMDT", currentDate);
  	      EXTCLS.set("EXCHNO", 0);
  	      EXTCLS.set("EXCHID", program.getUser());
          actionEXTCLS.insert(EXTCLS, recordExists);
    	  }
      }
    }
	
	  lstTestResults_Range01 = new ArrayList();
  		
    Map<String,String> headers01 = ["Accept": "application/json"];
    Map<String,String> params01 = [ "AGATNR":atnr, "maxrecs": "700"];
    String url01 = "/M3/m3api-rest/v2/execute/CMS100MI/Lst_ZATS100";
      
    IonResponse response01 = ion.get(url01, headers01, params01);
    if (response01.getStatusCode() == 200) {
      JsonSlurper jsonSlurper = new JsonSlurper();
      Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(response01.getContent());
      ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
       
      ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
      recordList.eachWithIndex { it, index ->
        Map<String, String> recordQMSTRS = (Map<String, String>) it;
        if (lstTestResults_Range01.size() < 500) {
    	    atid = recordQMSTRS.AGATID;
    	    atva = recordQMSTRS.AGATVA;
    	    atvn = recordQMSTRS.AGATVN;
    	    tx30 = recordQMSTRS.AATX30;
    	    tx15 = recordQMSTRS.PFTX15;
    	    tx31 = recordQMSTRS.PFTX30;
    	    unms = recordQMSTRS.AAUNMS;
    	  
    	    if(atid == 'DRF01' ) {
            DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'H');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", atva);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	       
    	    if(atid >= 'ADJ01' && atid <= 'ADJ99' && !atva.equals("N.")) {
    	          
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", atva);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	    if(atid == 'DRF03') {
    	      
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", atva);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'VAR01') {
    	          
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", atva);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'SIR01' && !atvn.equals("0.0000")) {
    	        
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	     
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	     }
    	    
    	     if(atid == 'SIR02' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	     
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'SIR03' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	     
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'SIR04' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	      
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'OVR01' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	     
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'GRE01' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
    	      
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	    
    	     if(atid == 'DAM01' && !atvn.equals("0.0000")) {
    	       
    	      unms = 'KG';
    	      def df = new DecimalFormat("#0.000")
            def formatted = df.format(atvn.toDouble() * 1000);
           
    	      DBAction actionEXTCLS = database.table("EXTCLS").build();
  	        DBContainer EXTCLS = actionEXTCLS.getContainer();
  	
            EXTCLS.set("EXCONO", XXCONO);
  	        EXTCLS.set("EXDIVI", divi);
  	        EXTCLS.set("EXBREF", bref);
  	        EXTCLS.set("EXITNO", itno);
  	        EXTCLS.set("EXCAT1", 'I');
  	        EXTCLS.set("EXREF1", atid + '- ' + tx30);
  	        EXTCLS.set("EXREF2", df.format(atvn.toDouble() * 1000) + ' ' + unms);
  	        EXTCLS.set("EXREF3", tx31);
  	        EXTCLS.set("EXRGDT", currentDate);
  	        EXTCLS.set("EXRGTM", currentTime);
  	        EXTCLS.set("EXLMDT", currentDate);
  	        EXTCLS.set("EXCHNO", 0);
  	        EXTCLS.set("EXCHID", program.getUser());
            actionEXTCLS.insert(EXTCLS, recordExists);
    	    }
    	  }
      }
    }
	  
	  DBAction actionEXTCLS = database.table("EXTCLS").build();
  	DBContainer EXTCLS = actionEXTCLS.getContainer();
	  
	  EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'F');
  	EXTCLS.set("EXREF1", 'SUPPLIER:');
  	EXTCLS.set("EXREF2", suno);
  	EXTCLS.set("EXREF3", sunm);
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
      
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'A');
  	EXTCLS.set("EXREF1", 'CLASSIFICATION REPORT');
  	EXTCLS.set("EXREF2", '');
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
    
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'G');
  	EXTCLS.set("EXREF1", 'ITEM NO:');
  	EXTCLS.set("EXREF2", itno);
  	EXTCLS.set("EXREF3", itds);
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
    
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'B');
  	EXTCLS.set("EXREF1", '.');
  	EXTCLS.set("EXREF2", '');
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
      
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'C');
  	EXTCLS.set("EXREF1", '.');
  	EXTCLS.set("EXREF2", '.');
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
        
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'D');
  	EXTCLS.set("EXREF1", 'SAMPLE DOCKET:');
  	EXTCLS.set("EXREF2", bref);
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
      
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXCAT1", 'E');
  	EXTCLS.set("EXREF1", 'DOCKET:');
  	EXTCLS.set("EXREF2", sudo);
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
    
    EXTCLS.set("EXCONO", XXCONO);
  	EXTCLS.set("EXDIVI", divi);
  	EXTCLS.set("EXBREF", bref);
  	EXTCLS.set("EXITNO", itno);
  	EXTCLS.set("EXCAT1", 'J');
  	EXTCLS.set("EXREF1", 'NO OF BINS:');
  	EXTCLS.set("EXREF2", nobins.toString());
  	EXTCLS.set("EXREF3", '');
  	EXTCLS.set("EXRGDT", currentDate);
  	EXTCLS.set("EXRGTM", currentTime);
  	EXTCLS.set("EXLMDT", currentDate);
  	EXTCLS.set("EXCHNO", 0);
  	EXTCLS.set("EXCHID", program.getUser());
    actionEXTCLS.insert(EXTCLS, recordExists);
  }
	/*
   * recordExists - return record already exists error message to the MI
   *
   */
	Closure recordExists = {
     mi.error("Record already exists");
	}
}