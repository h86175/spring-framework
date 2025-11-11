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

import org.jspecify.annotations.Nullable;

import org.springframework.util.ResourceUtils;

/**
 * 加载资源的策略接口（例如类路径或文件系统资源）。
 * {@link org.springframework.context.ApplicationContext} 需要提供此功能，
 * 并额外支持 {@link org.springframework.core.io.support.ResourcePatternResolver}。
 *
 * <p>{@link DefaultResourceLoader} 是一个可独立使用的实现，
 * 既可在 ApplicationContext 之外使用，也被 {@link ResourceEditor} 所使用。
 *
 * <p>在运行于 ApplicationContext 时，类型为 {@code Resource} 和 {@code Resource[]} 的
 * Bean 属性可以通过字符串进行填充，使用该上下文特定的资源加载策略。
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
public interface ResourceLoader {

	/** 从类路径加载时使用的伪 URL 前缀："classpath:"。 */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	/**
	 * 为指定的资源位置返回一个 {@code Resource} 句柄。
	 * <p>该句柄应始终是可复用的资源描述符，允许多次调用
	 * {@link Resource#getInputStream()}。
	 * <p><ul>
	 * <li>必须支持完全限定的 URL，例如："file:C:/test.dat"。
	 * <li>必须支持类路径伪 URL，例如："classpath:test.dat"。
	 * <li>应支持相对文件路径，例如："WEB-INF/test.dat"。
	 * （这取决于具体实现，通常由 ApplicationContext 的实现提供。）
	 * </ul>
	 * <p>注意：{@code Resource} 句柄并不意味着资源实际存在；
	 * 需要调用 {@link Resource#exists} 进行存在性检查。
	 * @param location 资源位置
	 * @return 对应的 {@code Resource} 句柄（不为 {@code null}）
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	Resource getResource(String location);

	/**
	 * 暴露此 {@code ResourceLoader} 所使用的 {@link ClassLoader}。
	 * <p>需要直接访问 {@code ClassLoader} 的客户端可以通过 {@code ResourceLoader}
	 * 以统一的方式进行访问，而无需依赖线程上下文 {@code ClassLoader}。
	 * @return 该 {@code ClassLoader}
	 * （仅当连系统 {@code ClassLoader} 都无法访问时才为 {@code null}）
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable ClassLoader getClassLoader();

}
