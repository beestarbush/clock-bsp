SUMMARY = "Boot partition image for RAUC A/B updates"
DESCRIPTION = "Produces a tar archive of the RPi5 boot partition contents \
(kernel, DTBs, overlays, config.txt, cmdline.txt) that RAUC can \
extract onto the target boot slot's FAT32 partition."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "^rpi$"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy nopackages

# We need the boot files to be deployed first
do_deploy[depends] += " \
    virtual/kernel:do_deploy \
    rpi-bootfiles:do_deploy \
    rpi-config:do_deploy \
"

BOOT_IMG_DIR = "${WORKDIR}/boot-image"

python do_collect_bootfiles() {
    """Collect all boot files into a staging directory."""
    import os, shutil, glob

    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE')
    bootfiles_dir = os.path.join(deploy_dir, d.getVar('BOOTFILES_DIR_NAME') or 'bootfiles')
    boot_img_dir = d.getVar('BOOT_IMG_DIR')

    # Clean and create staging dir
    if os.path.exists(boot_img_dir):
        shutil.rmtree(boot_img_dir)
    os.makedirs(boot_img_dir)

    # Copy firmware bootfiles (config.txt, etc.)
    if os.path.isdir(bootfiles_dir):
        for item in os.listdir(bootfiles_dir):
            src = os.path.join(bootfiles_dir, item)
            dst = os.path.join(boot_img_dir, item)
            if os.path.isfile(src):
                shutil.copy2(src, dst)
            elif os.path.isdir(src):
                shutil.copytree(src, dst)

    # Copy kernel image
    kernel_imagetype = d.getVar('KERNEL_IMAGETYPE') or 'Image'
    sdimg_kernelimage = d.getVar('SDIMG_KERNELIMAGE') or 'kernel_2712.img'

    kernel_src = os.path.join(deploy_dir, kernel_imagetype)
    if os.path.exists(kernel_src):
        shutil.copy2(kernel_src, os.path.join(boot_img_dir, sdimg_kernelimage))

    # Copy device tree files
    kernel_devicetree = (d.getVar('KERNEL_DEVICETREE') or '').split()
    for dtb in kernel_devicetree:
        base = os.path.basename(dtb)
        dtb_src = os.path.join(deploy_dir, base)
        if os.path.exists(dtb_src):
            if dtb.endswith('.dtbo') or base == 'overlay_map.dtb':
                # Overlay DTBs go into overlays/
                overlay_dir = os.path.join(boot_img_dir, 'overlays')
                os.makedirs(overlay_dir, exist_ok=True)
                shutil.copy2(dtb_src, os.path.join(overlay_dir, base))
            else:
                shutil.copy2(dtb_src, os.path.join(boot_img_dir, base))
}

do_deploy() {
    # Create a tar archive of the boot files for RAUC
    cd ${BOOT_IMG_DIR}
    tar cf ${DEPLOYDIR}/bee-boot-image-${MACHINE}.tar .

    # Create a symlink without the machine name for convenience
    ln -sf bee-boot-image-${MACHINE}.tar ${DEPLOYDIR}/bee-boot-image.tar
}

addtask collect_bootfiles before do_deploy after do_install
addtask deploy before do_build after do_collect_bootfiles

do_deploy[dirs] += "${DEPLOYDIR}"

# Ensure proper task ordering
do_collect_bootfiles[dirs] = "${BOOT_IMG_DIR}"
