/*
 * DrBookings
 *
 * Copyright (C) 2016 - 2018 Alexander Kerner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 */

package com.github.drbookings.model.data;

public class NamedImpl extends IDedImpl implements Named {

    public NamedImpl(final String name) {
	super(name);
    }

    /* (non-Javadoc)
     * @see com.github.drbookings.model.data.Named#getName()
     */
    @Override
    public String getName() {
	return super.getId();
    }

    @Override
    public String toString() {
	return getName();
    }

}
