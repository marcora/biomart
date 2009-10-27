package org.biomart.objects.helpers;

public enum Rdbs {
	MYSQL		("mysql"),
	ORACLE		("oracle"),
	POSTGRESQL	("postgresql");
	private String identifier = null;
	private Rdbs(String identifier) {
		this.identifier = identifier;
	}
	public boolean isMySql() {
		return Rdbs.MYSQL.equals(this);
	}
	public boolean isOracle() {
		return Rdbs.ORACLE.equals(this);
	}
	public boolean isPostgreSql() {
		return Rdbs.POSTGRESQL.equals(this);
	}
	public static Rdbs fromString(String identifer) {
		for (Rdbs rdbs : values()) {
			if (rdbs.identifier.equals(identifer)) {
				return rdbs;
			}
		}
		return null;
	}
}
