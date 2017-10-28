package org.minimalj.frontend.page;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class PageTest {

	@Test
	public void nullRouteNotValid() {
		Assert.assertFalse(Page.validateRoute(null));
	}

	@Test
	public void emptyRouteNotValid() {
		Assert.assertFalse(Page.validateRoute(""));
	}

	@Test
	public void blankRouteNotValid() {
		Assert.assertFalse(Page.validateRoute(" "));
	}

	@Test
	public void slashRouteNotValid() {
		Assert.assertFalse(Page.validateRoute("/"));
	}

	@Test
	public void startWithSlashRouteNotValid() {
		Assert.assertFalse(Page.validateRoute("/a"));
	}

	@Test
	public void endWithSlashRouteNotValid() {
		Assert.assertFalse(Page.validateRoute("a/"));
	}

	@Test
	public void slashInMiddleRouteValid() {
		Assert.assertTrue(Page.validateRoute("a/b"));
	}

	@Test
	public void pathsNotValid() {
		Assert.assertFalse(Page.validateRoute("a./b"));
		Assert.assertFalse(Page.validateRoute("a/../b"));
	}

	
	@Test
	public void validCharactersRouteValid() {
		Assert.assertTrue(Page.validateRoute("a._-/b"));
	}

	@Test
	public void invalidCharactersRouteNotValid() {
		Assert.assertFalse(Page.validateRoute("ä"));
	}
	
	@Test
	public void uuidRouteNotValid() {
		Assert.assertTrue(Page.validateRoute("class/" + UUID.randomUUID()));
	}

}
