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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import org.springframework.util.FileCopyUtils;

/**
 * 用于资源描述符的接口，它抽象了底层资源的实际类型，例如文件或类路径资源。
 *
 * <p>如果资源以物理形式存在，则可以为每个资源打开一个 InputStream，
 * 但对于某些资源，只能返回 URL 或 File 句柄。实际行为是特定于实现的。
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see FileUrlResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
public interface Resource extends InputStreamSource {

	/**
	 * 判断此资源是否以物理形式实际存在。
	 * <p>此方法执行明确的存在性检查，而 {@code Resource} 句柄的存在仅保证描述符句柄有效。
	 */
	boolean exists();

	/**
	 * 指示是否可以通过 {@link #getInputStream()} 读取此资源的非空内容。
	 * <p>对于存在的典型资源描述符，将为 {@code true}，因为它严格地暗示了自 5.1 版本以来的 {@link #exists()} 语义。
	 * 请注意，尝试读取实际内容时仍可能失败。
	 * 但是，值为 {@code false} 则明确表示无法读取资源内容。
	 * @see #getInputStream()
	 * @see #exists()
	 */
	default boolean isReadable() {
		return exists();
	}

	/**
	 * 指示此资源是否表示具有开放流的句柄。
	 * 如果为 {@code true}，则 InputStream 不能被多次读取，
	 * 并且必须被读取并关闭以避免资源泄漏。
	 * <p>对于典型的资源描述符，将为 {@code false}。
	 */
	default boolean isOpen() {
		return false;
	}

	/**
	 * 判断此资源是否表示文件系统中的文件。
	 * <p>值为 {@code true} 表明（但不保证）{@link #getFile()} 调用将成功。
	 * 对于非默认文件系统，{@link #getFilePath()} 是更可靠的后续调用。
	 * <p>默认情况下，此值为保守的 {@code false}。
	 * @since 5.0
	 * @see #getFile()
	 * @see #getFilePath()
	 */
	default boolean isFile() {
		return false;
	}

	/**
	 * 返回此资源的 URL 句柄。
	 * @throws IOException 如果资源无法解析为 URL，
	 * 即，如果资源不可用作描述符
	 */
	URL getURL() throws IOException;

	/**
	 * 返回此资源的 URI 句柄。
	 * @throws IOException 如果资源无法解析为 URI，
	 * 即，如果资源不可用作描述符
	 * @since 2.5
	 */
	URI getURI() throws IOException;

	/**
	 * 返回此资源的 File 句柄。
	 * <p>注意：这仅适用于默认文件系统中的文件。
	 * @throws UnsupportedOperationException 如果资源是文件但无法作为 {@code java.io.File} 公开；请尝试改用 {@link #getFilePath()}
	 * @throws java.io.FileNotFoundException 如果资源无法解析为文件
	 * @throws IOException 在发生常规解析/读取失败时
	 * @see #getInputStream()
	 */
	File getFile() throws IOException;

	/**
	 * 返回此资源的 NIO Path 句柄。
	 * <p>注意：这也适用于非默认文件系统中的文件。
	 * @throws java.io.FileNotFoundException 如果资源无法解析为文件
	 * @throws IOException 在发生常规解析/读取失败时
	 * @since 7.0
	 */
	default Path getFilePath() throws IOException {
		return getFile().toPath();
	}

	/**
	 * 返回一个 {@link ReadableByteChannel}。
	 * <p>期望每次调用都会创建一个<i>新</i>的通道。
	 * <p>默认实现返回带有 {@link #getInputStream()} 结果的 {@link Channels#newChannel(InputStream)}。
	 * @return 底层资源的字节通道（不能为 {@code null}）
	 * @throws java.io.FileNotFoundException 如果底层资源不存在
	 * @throws IOException 如果无法打开内容通道
	 * @since 5.0
	 * @see #getInputStream()
	 */
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	/**
	 * 以字节数组形式返回此资源的内容。
	 * @return 此资源的内容（字节数组）
	 * @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，
	 * 即，如果资源在文件系统中不可用
	 * @throws IOException 在发生常规解析/读取失败时
	 * @since 6.0.5
	 */
	default byte[] getContentAsByteArray() throws IOException {
		return FileCopyUtils.copyToByteArray(getInputStream());
	}

	/**
	 * 使用指定的字符集将此资源的内容作为字符串返回。
	 * @param charset 用于解码的字符集
	 * @return 此资源的内容（{@code String}）
	 * @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，
	 * 即，如果资源在文件系统中不可用
	 * @throws IOException 在发生常规解析/读取失败时
	 * @since 6.0.5
	 */
	default String getContentAsString(Charset charset) throws IOException {
		return FileCopyUtils.copyToString(new InputStreamReader(getInputStream(), charset));
	}

	/**
	 * 确定此资源的内容长度。
	 * @throws IOException 如果资源无法解析
	 * （在文件系统中或作为其他已知的物理资源类型）
	 */
	long contentLength() throws IOException;

	/**
	 * 确定此资源的最后修改时间戳。
	 * @throws IOException 如果资源无法解析
	 * （在文件系统中或作为其他已知的物理资源类型）
	 */
	long lastModified() throws IOException;

	/**
	 * 创建一个相对于此资源的资源。
	 * @param relativePath 相对路径（相对于此资源）
	 * @return 相对资源的资源句柄
	 * @throws IOException 如果无法确定相对资源
	 */
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * 确定此资源的-文件名 — 通常是路径的最后一部分 — 例如，{@code "myfile.txt"}。
	 * <p>如果此类型的资源没有文件名，则返回 {@code null}。
	 * <p>鼓励实现返回未编码的文件名。
	 */
	@Nullable String getFilename();

	/**
	 * 返回此资源的描述，
	 * 用于处理资源时输出错误信息。
	 * <p>也鼓励实现从其 {@code toString} 方法返回此值。
	 * @see Object#toString()
	 */
	String getDescription();

}
