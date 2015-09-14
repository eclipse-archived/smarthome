def bindings = new File("$project.basedir", 'documentation/features/bindings')
bindings.eachFile {
	def name = it.name
	if(name.contains('binding')) {
		def bindingId = it.name.replace('org.eclipse.smarthome.binding.', '')
		def simpleBindingNameDir = new File(bindings.path, bindingId)
		it.renameTo(simpleBindingNameDir)
		def readme = new File(simpleBindingNameDir.path, 'README.md')
		if(readme.exists()) {
			println readme
			readme.renameTo(new File(simpleBindingNameDir.path, 'readme.md'))
		}
	}
}