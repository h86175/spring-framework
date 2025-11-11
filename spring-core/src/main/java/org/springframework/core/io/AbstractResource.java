/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ResourceUtils;

/**
 * {@link Resource} 实现的便捷基类，
 * 预先实现了常见的典型行为。
 *
 * <p>“exists” 方法会检查是否能打开 File 或 InputStream；
 * “isOpen” 始终返回 false；“getURL” 与 “getFile” 会抛出异常；
 * “toString” 返回资源的描述信息。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 28.12.2003
 */
public abstract class AbstractResource implements Resource {

	/**
	 * 此实现会先检查是否能打开一个 File，
	 * 如果不行则回退到检查是否能打开一个 InputStream。
	 * <p>这既可覆盖目录，也可覆盖具体内容资源。
	 */
	@Override
	public boolean exists() {
		// 尝试文件存在性检查：能否在文件系统中找到该文件？
		if (isFile()) {
			try {
				return getFile().exists();
			}
			catch (IOException ex) {
				debug(() -> "无法获取 File 以检查存在性：" + getDescription(), ex);
			}
		}
		// 回退到流存在性检查：我们能否打开该流？
		try {
			getInputStream().close();
			return true;
		}
		catch (Throwable ex) {
			debug(() -> "无法获取 InputStream 以检查存在性：" + getDescription(), ex);
			return false;
		}
	}

	/**
	 * 此实现在资源 {@link #exists() 存在} 时始终返回 {@code true}
	 *（5.1 起修订）。
	 */
	@Override
	public boolean isReadable() {
		return exists();
	}

	/**
	 * 此实现始终返回 {@code false}。
	 */
	@Override
	public boolean isOpen() {
		return false;
	}

	/**
	 * 此实现始终返回 {@code false}。
	 */
	@Override
	public boolean isFile() {
		return false;
	}

	/**
	 * 此实现抛出 FileNotFoundException，
	 * 假定该资源无法解析为 URL。
	 */
	@Override
	public URL getURL() throws IOException {
		throw new FileNotFoundException(getDescription() + " 无法解析为 URL");
	}

	/**
	 * 此实现基于 {@link #getURL()} 返回的 URL 构建一个 URI。
	 */
	@Override
	public URI getURI() throws IOException {
		URL url = getURL();
		try {
			return ResourceUtils.toURI(url);
		}
		catch (URISyntaxException ex) {
			throw new IOException("非法的 URI [" + url + "]", ex);
		}
	}

	/**
	 * 此实现抛出 FileNotFoundException，
	 * 假定该资源无法解析为绝对文件路径。
	 */
	@Override
	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " 无法解析为绝对文件路径");
	}

	/**
	 * 此实现返回 {@link Channels#newChannel(InputStream)}，
	 * 通道的来源是 {@link #getInputStream()} 的结果。
	 * <p>这与 {@link Resource} 对应默认方法的行为一致，
	 * 在类层级中镜像此实现以获得更高效的 JVM 级分派。
	 */
	@Override
	public ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	/**
	 * 此方法通过读取整个 InputStream 来确定内容长度。
	 * <p>对于 {@code InputStreamResource} 的自定义子类，
	 * 强烈建议重写此方法以采用更优的实现，比如检查文件长度，
	 * 或者在流只能读取一次时，直接返回 -1。
	 * @see #getInputStream()
	 */
	@Override
	public long contentLength() throws IOException {
		InputStream is = getInputStream();
		try {
			long size = 0;
			byte[] buf = new byte[256];
			int read;
			while ((read = is.read(buf)) != -1) {
				size += read;
			}
			return size;
		}
		finally {
			try {
				is.close();
			}
			catch (IOException ex) {
				debug(() -> "无法关闭用于计算内容长度的 InputStream：" + getDescription(), ex);
			}
		}
	}

	/**
	 * 此实现检查底层 File 的时间戳（如果可用）。
	 * @see #getFileForLastModifiedCheck()
	 */
	@Override
	public long lastModified() throws IOException {
		File fileToCheck = getFileForLastModifiedCheck();
		long lastModified = fileToCheck.lastModified();
		if (lastModified == 0L && !fileToCheck.exists()) {
			throw new FileNotFoundException(getDescription() +
					" 无法在文件系统中解析，因而无法检查其最后修改时间戳");
		}
		return lastModified;
	}

	/**
	 * 确定用于时间戳检查的 File。
	 * <p>默认实现委托给 {@link #getFile()}。
	 * @return 用于时间戳检查的 File（永不为 {@code null}）
	 * @throws FileNotFoundException 如果资源无法解析为绝对文件路径，
	 * 即不在文件系统中可用
	 * @throws IOException 一般解析或读取失败时抛出
	 */
	protected File getFileForLastModifiedCheck() throws IOException {
		return getFile();
	}

	/**
	 * 此实现抛出 FileNotFoundException，
	 * 假定无法为该资源创建相对资源。
	 */
	@Override
	public Resource createRelative(String relativePath) throws IOException {
		throw new FileNotFoundException("无法为该资源创建相对资源：" + getDescription());
	}

	/**
	 * 此实现始终返回 {@code null}，
	 * 假定该资源类型没有文件名。
	 */
	@Override
	public @Nullable String getFilename() {
		return null;
	}

	/**
	 * 在异常时延迟获取 logger 以进行 debug 日志输出。
	 */
	private void debug(Supplier<String> message, Throwable ex) {
		Log logger = LogFactory.getLog(getClass());
		if (logger.isDebugEnabled()) {
			logger.debug(message.get(), ex);
		}
	}


	/**
	 * 此实现比较描述字符串。
	 * @see #getDescription()
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof Resource that &&
				getDescription().equals(that.getDescription())));
	}

	/**
	 * 此实现返回描述字符串的哈希值。
	 * @see #getDescription()
	 */
	@Override
	public int hashCode() {
		return getDescription().hashCode();
	}

	/**
	 * 此实现返回该资源的描述。
	 * @see #getDescription()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}
