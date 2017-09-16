package org.minimalj.example.demo;

import java.net.URI;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // this was just to find out how URI works
public class UriExamplesTest {

	@Test
	public void uriTest() {
		URI uri = URI.create("http://localhost/app1/person/2");
		Assert.assertEquals("/app1/person/2", uri.getPath());
		
		uri = URI.create("http://localhost/app1/person/2#a=b");
		Assert.assertEquals("/app1/person/2", uri.getPath());
		Assert.assertEquals("a=b", uri.getFragment());

		String[] pathElements = uri.getPath().split("/");
		Assert.assertEquals("", pathElements[0]);
		Assert.assertEquals("app1", pathElements[1]);
		Assert.assertEquals("person", pathElements[2]);
		
		uri = URI.create("/person/2");
		Assert.assertEquals("/person/2", uri.getPath());
	}
}
