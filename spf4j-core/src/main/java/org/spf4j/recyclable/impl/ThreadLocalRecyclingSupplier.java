/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.recyclable.impl;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.spf4j.recyclable.NonValidatingRecyclingSupplier;

/**
 * @author Zoltan Farkas
 */
public final class ThreadLocalRecyclingSupplier<T> implements NonValidatingRecyclingSupplier<T> {

  private final Supplier<T> supplier;

  private final ThreadLocal<T> threadLocal;

  public ThreadLocalRecyclingSupplier(final Supplier<T> supplier) {
    this.supplier = supplier;
    threadLocal = new ThreadLocal<T>() {
      @Override
      protected T initialValue() {
        return supplier.get();
      }
    };
  }

  @Override
  @Nonnull
  public T get() {
    T obj = threadLocal.get();
    if (obj == null) {
      return supplier.get();
    } else {
      threadLocal.set(null);
      return obj;
    }
  }

  @Override
  public void recycle(final T object) {
    threadLocal.set(object);
  }

  @Override
  public boolean tryDispose(final long timeoutMillis) {
    return true;
  }

  @Override
  public String toString() {
    return "ThreadLocalRecyclingSupplier{" + "supplier=" + supplier + ", threadLocal=" + threadLocal + '}';
  }



}
