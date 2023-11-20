package com.project.one.server;

public class ProjectEnums {
	public enum ConnectionType {
		UDP,
		TCP,
		ALL
	};
	public enum MethodType {
		PUT,
		GET,
		DELETE,
		STOP,
		RUN
	}
	public enum RequestKeys {
		type,
		data,
	}
}