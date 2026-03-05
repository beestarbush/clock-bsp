FILESEXTRAPATHS:prepend := "${THISDIR}/rauc:"

SRC_URI:append = " \
    file://rpi-tryboot-backend.sh \
    file://rauc-mark-good.service \
"

# Enable the RAUC D-Bus service for CLI interaction
PACKAGECONFIG:append = " service"

do_install:append() {
    # Install the custom tryboot backend script
    install -d ${D}/usr/lib/rauc
    install -m 0755 ${WORKDIR}/rpi-tryboot-backend.sh ${D}/usr/lib/rauc/rpi-tryboot-backend.sh

    # Install the auto mark-good service
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/rauc-mark-good.service ${D}${systemd_unitdir}/system/rauc-mark-good.service
}

inherit systemd

SYSTEMD_SERVICE:${PN}:append = " rauc-mark-good.service"
SYSTEMD_AUTO_ENABLE = "enable"

FILES:${PN} += " \
    /usr/lib/rauc/rpi-tryboot-backend.sh \
    ${systemd_unitdir}/system/rauc-mark-good.service \
"
