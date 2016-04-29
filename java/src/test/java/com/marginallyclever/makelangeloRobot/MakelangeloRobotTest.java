package com.marginallyclever.makelangeloRobot;

import org.junit.Test;

public class MakelangeloRobotTest {
	@Test
	public void checkGUID() throws IllegalStateException {
		if(MakelangeloRobot.please_get_a_guid==false) throw new IllegalStateException("Turn please_get_a_guid to true before release!");
	}
}
