#!/usr/bin/python
# -*- coding: iso-8859-15 -*-

import ssl

def makeDefaultSSLContext():
	ctx = ssl.create_default_context()
	ctx.check_hostname = False
	ctx.verify_mode = ssl.CERT_NONE
	#ctx.verify_mode = ssl.CERT_REQUIRED
	return ctx
