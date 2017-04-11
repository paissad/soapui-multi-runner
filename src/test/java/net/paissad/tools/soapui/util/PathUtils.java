package net.paissad.tools.soapui.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PathUtils {

	private PathUtils() {
	}

	public static void setReadable(final Path path, boolean readable) throws IOException {
		if (isWindows()) {
			// Set permissions using DOS

			final UserPrincipal currentUser = path.getFileSystem().getUserPrincipalLookupService()
					.lookupPrincipalByName(System.getProperty("user.name"));

			// get view
			final AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);

			// create ACE to set read access for the current user
			final AclEntryType entryType = readable ? AclEntryType.ALLOW : AclEntryType.DENY;
			final AclEntry entry = AclEntry.newBuilder().setType(entryType).setPrincipal(currentUser)
					.setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES).build();

			// read ACL, insert ACE, re-write ACL
			final List<AclEntry> acl = view.getAcl();
			acl.add(0, entry); // insert before any DENY entries
			view.setAcl(acl);

		} else {
			// Set permissions using POSIX
			final Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
			if (readable) {
				perms.add(PosixFilePermission.OWNER_READ);
				perms.add(PosixFilePermission.GROUP_READ);
				perms.add(PosixFilePermission.OTHERS_READ);
			} else {
				perms.remove(PosixFilePermission.OWNER_READ);
				perms.remove(PosixFilePermission.GROUP_READ);
				perms.remove(PosixFilePermission.OTHERS_READ);
			}
			Files.setPosixFilePermissions(path, perms);
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
	}

}
