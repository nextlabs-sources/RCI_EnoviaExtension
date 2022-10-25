package com.nextlabs.enovia.extension.impl;

public class RCIExtensionConstant {
	
	private RCIExtensionConstant() {};

	// key name of citizenship confirmed
	public static final String CITIZENSHIP_CONFIRMED = "user-citizenship-confirmed";
	public static final boolean CITIZENSHIP_CONFIRMED_DEFAULT_VALUE = false;
	public static final boolean WQ_CITIZENSHIP_CONFIRMED_DEFAULT_VALUE = false;
	public static final int CITIZENSHIP_CONFIRMED_SUCCESS = 0;
	
	// key name of structure valid
	public static final String STRUCTURE_VALID = "structure-valid";
	
	// key name of export control
	public static final String EXPORT_CONTROL = "export-control";
	
	// part number
	public static final String PART_NUMBER = "name";
	
	// vault
	public static final String VAULT = "vault";
	
	// event id
	public static final String EVENT_ID = "event-id";
	
	// classification api service exception
	public static final String CLASSIFICATION_API_EXCEPTION = "classification-api-exception";
	
	// part classification items
	public static final String MATERIAL_GROUP = "MaterialGroup";
	public static final String COUNTRY = "Country";
	public static final String REGULATION = "Regulation";
	public static final String ECCN = "ECCN";
	public static final String GROUPING = "Grouping";
	public static final String SCCODE = "SCCode";
	public static final String DESIGN_COG = "DesignCog";
	public static final String PDM_MASTER_VAULT = "PDMMasterVault";
	public static final String US_LEGAL_CONFIRMED = "US_LegalConfirmed";
	public static final String UK_LEGAL_CONFIRMED = "UK_LegalConfirmed"; 
	public static final String FR_LEGAL_CONFIRMED = "FR_LegalConfirmed";
	public static final String DE_LEGAL_CONFIRMED = "DE_LegalConfirmed"; 
	public static final String AU_LEGAL_CONFIRMED = "AU_LegalConfirmed";
	public static final String CA_LEGAL_CONFIRMED = "CA_LegalConfirmed";
	public static final String SG_LEGAL_CONFIRMED = "SG_LegalConfirmed";
	public static final String NZ_LEGAL_CONFIRMED = "NZ_LegalConfirmed";
	public static final String CN_LEGAL_CONFIRMED = "CN_LegalConfirmed";
	public static final String JP_LEGAL_CONFIRMED = "JP_LegalConfirmed";
	public static final String NL_LEGAL_CONFIRMED = "NL_LegalConfirmed";
	
	// part license items
	public static final String LICENSE_NUMBER = "LicenseNumber";
	public static final String LICENSE_TYPE = "LicenseType";
	public static final String COMPANY_CODE = "CompanyCode";
    public static final String PERSONNEL_AREA = "PersonnelArea";
    public static final String EXPIRATION_DATE = "ExpirationDate";
    
    public static final String EXPIRED_LICENSE_NUMBER = "ExpiredLicenseNumber";
    
    // part contract items
    public static final String SALES_ORG = "SalesOrg";
    public static final String DISTRIBUTION_CHANNEL = "DistributionChannel";
    public static final String SALES_ORDER_TYPE = "SalesOrderType";
    public static final String CONTRACT_NUMBER = "ContractNumber";
    public static final String END_DATE = "EndDate";
    public static final String REJECT = "Reject";
	
    // obsolete
    // LOG4J configuration file
  	public static final String LOG4J_CONFIG_FILE = "log4j_rciext.properties";
  	
  	// classification filter criteria
  	public static final String FILTER_COUNTRY = "US";
  	public static final String FILTER_REGULATION = "TD";
  	
  	// user name of web query
  	// no citizenship confirmed for web query user
  	public static final String WQ_USERNAME = "WebQuery Admin";
  	
  	// connection properties to classification system
  	public static final String CLASSIFICATION_CONN_PROPERTIES = "classification_conn.properties";
  	public static String HOST_NAME = null;
	public static String CLIENT_ID = "1";
	public static String USER_NAME = "xgsa3852";
	public static String PASSWORD = "Xr!93Tx@;SAf2Vd";
	public static String ENCRYPTIONSECRETKEY = "";
    
}
