package it.cammino.risuscito.utils

import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem

/** Set the name */
var ProfileDrawerItem.nameText: String?
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not readable")
    get() = throw UnsupportedOperationException("Please use the direct property")
    set(value) {
        name = StringHolder(value)
    }

/** Set the description */
var ProfileDrawerItem.descriptionText: String?
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not readable")
    get() = throw UnsupportedOperationException("Please use the direct property")
    set(value) {
        description = StringHolder(value)
    }

/** Set the name */
var ProfileSettingDrawerItem.nameRes: Int
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not readable")
    get() = throw UnsupportedOperationException("Please use the direct property")
    set(value) {
        name = StringHolder(value)
    }