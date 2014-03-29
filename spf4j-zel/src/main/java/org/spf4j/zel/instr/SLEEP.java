/*
 * Copyright (c) 2001, Zoltan Farkas All Rights Reserved.
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
 */
package org.spf4j.zel.instr;

import org.spf4j.base.Arrays;
import org.spf4j.zel.vm.ExecutionContext;
import org.spf4j.zel.vm.SuspendedException;
import org.spf4j.zel.vm.ZExecutionException;


/**
 *
 * @author zoly
 */
public final class SLEEP extends Instruction {

    private static final long serialVersionUID = -104479947741779060L;

    private SLEEP() {
    }

    @Override
    public int execute(final ExecutionContext context)
            throws ZExecutionException, SuspendedException {
        try {
            Number param = (Number) context.popSyncStackVal();
            Thread.sleep(param.longValue());
            return 1;
        } catch (InterruptedException ex) {
            throw new ZExecutionException("sleeping interrupted", ex, context);
        }
    }
    /**
     * instance
     */
    public static final Instruction INSTANCE = new SLEEP();

    @Override
    public Object[] getParameters() {
        return Arrays.EMPTY_OBJ_ARRAY;
    }


}
