# Point rauc-conf at our project-specific system.conf and keyring certificate
FILESEXTRAPATHS:prepend := "${THISDIR}/rauc:${THISDIR}/rauc-certs:"

RAUC_KEYRING_FILE = "ca.cert.pem"
