package com.yyc.common;

/**
     * AccessToken的数据模型
     */
    public  class AccessToken {

        //获取到的凭证
        private String accessToken;
        //凭证有效时间，单位：秒
        private int expiresin;

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setExpiresin(int expiresin) {
            this.expiresin = expiresin;
        }
    }
