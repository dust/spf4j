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
import org.spf4j.zel.operators.Operator;
import org.spf4j.zel.operators.Operators;
import org.spf4j.zel.vm.ExecutionContext;
import org.spf4j.zel.vm.SuspendedException;


/**
 * Add takes two objects from the top of the stack and puts the sum back
 *
 * @author zoly
 * @version 1.0
 */
public final class ADD extends Instruction {

    private static final long serialVersionUID = 6127414006563169983L;

    private ADD() {
    }
    
    /**
     * ADD Instruction microcode
     * if any of the operands are null the result is null
     * @param context ExecutionContext
     */
    @Override
    public int execute(final ExecutionContext context)
            throws SuspendedException {
        final Object[] vals = context.popSyncStackVals(2);
        context.push(Operators.apply(Operator.Enum.Add, vals[0], vals[1]));
        return 1;
    }
    /**
     * Add instance
     */
    public static final Instruction INSTANCE = new ADD();

    @Override
    public Object[] getParameters() {
        return Arrays.EMPTY_OBJ_ARRAY;
    }


}
