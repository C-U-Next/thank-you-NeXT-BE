package com.develop.thankyounext.global.exception.handler;

import com.develop.thankyounext.global.exception.GeneralException;
import com.develop.thankyounext.global.payload.code.BaseErrorCode;

public class TokenHandler extends GeneralException {

    public TokenHandler(BaseErrorCode code) {
        super(code);
    }
}
