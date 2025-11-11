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

import java.io.IOException;
import java.io.InputStream;

/**
 * 为可以提供 {@link InputStream} 的对象定义的简单接口。
 *
 * <p>这是 Spring 更为完善的 {@link Resource} 接口的基础接口。
 *
 * <p>对于一次性使用的流，可以使用 {@link InputStreamResource}
 * 来包装任意给定的 {@code InputStream}。而 Spring 的
 * {@link ByteArrayResource} 或任何基于文件的 {@code Resource}
 * 实现都可以作为具体实例，使得可以多次读取底层内容流。
 * 例如，这使得该接口可作为邮件附件的抽象内容来源。
 *
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see java.io.InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
@FunctionalInterface
public interface InputStreamSource {

	/**
	 * 返回底层资源内容的 {@link InputStream}。
	 * <p>通常期望每次调用都会创建一个<i>新的</i>输入流。
	 * <p>这一点在使用诸如 JavaMail 的 API 时尤为重要，
	 * 例如在创建邮件附件时需要能够多次读取该流。
	 * 因此，在这种用例中，<i>必须</i>确保每次调用
	 * {@code getInputStream()} 都返回一个新的流。
	 * @return 底层资源的输入流（不得为 {@code null}）
	 * @throws java.io.FileNotFoundException 如果底层资源不存在
	 * @throws IOException 如果内容流无法打开
	 * @see Resource#isReadable()
	 * @see Resource#isOpen()
	 */
	InputStream getInputStream() throws IOException;

}
