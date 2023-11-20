package com.project.one;

import java.io.IOException;

public class ServerDebug {

	public static void main(String[] args) throws IOException, InterruptedException {
//		args = new String[] {"TCP","localhost","40000"};
//		args = new String[] {"UDP","localhost","40000"};
//		args = new String[] {"ALL","localhost","40000","40001"};
		ServerRun.run(args);
	}

}
