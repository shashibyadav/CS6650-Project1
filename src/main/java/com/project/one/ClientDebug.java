package com.project.one;

import java.io.IOException;

public class ClientDebug {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
//		args = new String[] {"TCP","localhost","40000"};
//		args = new String[] {"UDP","localhost","40000"};
		ClientRun.run(args);
	}

}
