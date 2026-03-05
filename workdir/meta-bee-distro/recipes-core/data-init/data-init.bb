SUMMARY = "Data partition initialization service"
DESCRIPTION = "Systemd service that initializes the persistent data partition \
directory structure on first boot, copying default configuration files."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://data-init.service \
    file://data-init.sh \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "data-init.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    # Install the init script
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/data-init.sh ${D}${bindir}/data-init.sh

    # Install the systemd service
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/data-init.service ${D}${systemd_unitdir}/system/data-init.service
}

FILES:${PN} = " \
    ${bindir}/data-init.sh \
    ${systemd_unitdir}/system/data-init.service \
"
