package com.boot.jx.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonFile extends CommonFileAbstract<CommonFile> implements MultipartFile {
	private static final long serialVersionUID = -254665668785648863L;

	@JsonIgnore
	public boolean hasTempFile() {
		return tempPath != null && Files.exists(tempPath);
	}

	@JsonIgnore
	public void deleteTempFile() {
		if (tempPath != null) {
			try {
				Files.deleteIfExists(tempPath);
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	public String getOriginalFilename() {
		return this.getName();
	}

	@Override
	public boolean isEmpty() {
		return tempPath == null || !Files.exists(tempPath) || contentLength == 0;
	}

	@Override
	public long getSize() {
		if (contentLength > 0) {
			return contentLength;
		}

		if (tempPath != null) {
			try {
				return Files.size(tempPath);
			} catch (IOException e) {
				return 0;
			}
		}

		return body != null ? body.length : 0;
	}

	@Override
	public byte[] getBytes() throws IOException {
		if (body != null) {
			return body;
		}

		if (tempPath != null) {
			return Files.readAllBytes(tempPath);
		}

		return new byte[0];
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (tempPath != null) {
			return Files.newInputStream(tempPath);
		}

		if (body != null) {
			return new ByteArrayInputStream(body);
		}

		throw new FileNotFoundException("No backing file available");
	}

	@Override
	public void transferTo(File dest) throws IOException {

		if (tempPath != null) {
			Files.copy(tempPath, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return;
		}

		if (body != null) {
			Files.write(dest.toPath(), body);
			return;
		}

		throw new FileNotFoundException("No backing file available");
	}

}
