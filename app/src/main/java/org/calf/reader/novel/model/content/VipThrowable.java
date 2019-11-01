package org.calf.reader.novel.model.content;

import org.calf.reader.novel.MApplication;
import org.calf.reader.novel.R;

public class VipThrowable extends Throwable {

    private final static String tag = "VIP_THROWABLE";

    VipThrowable() {
        super(MApplication.getInstance().getString(R.string.donate_s));
    }
}
