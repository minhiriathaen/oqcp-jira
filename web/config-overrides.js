const createEntries = require('react-app-rewire-multiple-entry');

const entries = createEntries([
    {
        entry: 'src/edit-account-mapping/index.tsx',
        template: 'public/index.html',
        outPath: '/edit-account-mapping.html'
    },
    {
        entry: 'src/edit-user-mapping/index.tsx',
        template: 'public/index.html',
        outPath: '/edit-user-mapping.html'
    },
    {
        entry: 'src/edit-project-mapping/index.tsx',
        template: 'public/index.html',
        outPath: '/edit-project-mapping.html'
    },
    {
        entry: 'src/view-open-quality-checker/index.tsx',
        template: 'public/index.html',
        outPath: '/view-open-quality-checker.html'
    },
    {
        entry: 'src/maintainability-dashboard-gadget/index.tsx',
        template: 'public/index.html',
        outPath: '/maintainability-dashboard-gadget.html'
    },
    {
        entry: 'src/health/index.tsx',
        template: 'src/health/index.html',
        outPath: '/health'
    }
]);

module.exports = function override(config, env) {
    entries.addMultiEntry(config);
    return config;
}