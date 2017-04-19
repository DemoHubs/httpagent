package com.onbet.httpagent.utils;

import java.io.File;
import java.util.Map;

public class HttpParam {

	private String url;
	private Map<String, Object> params;
	//报文的方式
	private String content;
	private Map<String, String> header;
	private String charset;
	private Map<String, File[]> files;
	private String method;
	private int timeout=5000;
	public HttpParam() {

	}
	public HttpParam(String url) {
		super();
		this.url = url;
	}

	public HttpParam(String url, Map<String, Object> params) {
		super();
		this.url = url;
		this.params = params;
	}

	public static class Proxy {
		private String host;
		private int port;
		private String password;

		public Proxy(String host, int port) {
			super();
			this.host = host;
			this.port = port;
		}

		public Proxy(String host, int port, String password) {
			super();
			this.host = host;
			this.port = port;
			this.password = password;
		}
		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Map<String, File[]> getFiles() {
		return files;
	}

	public void setFiles(Map<String, File[]> files) {
		this.files = files;
	}

}
