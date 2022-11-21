package com.editran.Repository;

public enum ApiTransaccionDataBase {
    
    LOG_P_REGISTRAR("LOGSULL", "", "LOG_P_REGISTRAR"),
    BC_TIN_P_GENERA_ARCHIVOS("CCESULL", "BC_TIN_K_ARCHIVOS", "BC_TIN_P_GENERA_ARCHIVOS"),
    BC_TIN_P_PROCESA_ARCHIVOS("CCESULL", "BC_TIN_K_ARCHIVOS", "BC_TIN_P_PROCESA_ARCHIVOS"),
    BC_TIN_P_PROCESA_ARCHIVOS_RC("CCESULL", "", "BC_TIN_P_PROCESA_ARCHIVOS_RC"),
    BC_TIN_P_PROCESA_ENTRANTES("CCESULL", "", "BC_TIN_P_PROCESA_ENTRANTES"),
    BC_TIN_P_INSERTA_TRAMA("CCESULL", "BC_TIN_K_ARCHIVOS", "BC_TIN_P_INSERTA_TRAMA");

	private final String Esquema;
	private final String PackageDba;
	private final String StoreProcedure;

	private ApiTransaccionDataBase(String esquema, String packageDba, String storeProcedure) {
		Esquema = esquema;
		PackageDba = packageDba;
		StoreProcedure = storeProcedure;
	}

	public String getEsquema() {
		return Esquema;
	}

	public String getPackageDba() {
		return PackageDba;
	}

	public String getStoreProcedure() {
		return StoreProcedure;
	}
}
