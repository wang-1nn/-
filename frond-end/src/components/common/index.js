// 导入基础组件
import Button from './Button.vue';
import Card from './Card.vue';
import Input from './Input.vue';
import Alert from './Alert.vue';
import Badge from './Badge.vue';
import Tabs from './Tabs.vue';
import TabPane from './TabPane.vue';
import TabNav from './TabNav.vue';
import ContentContainer from './ContentContainer.vue';

// 导入布局组件
import Layout from '../layout/Layout.vue';

// 导出所有组件
export {
    // 基础组件
    Button,
    Card,
    Input,
    Alert,
    Badge,
    Tabs,
    TabPane,
    TabNav,
    ContentContainer,

    // 布局组件
    Layout
};

// 导出组件插件
export default {
    install(app) {
        // 注册所有组件
        app.component('UiButton', Button);
        app.component('UiCard', Card);
        app.component('UiInput', Input);
        app.component('UiAlert', Alert);
        app.component('UiBadge', Badge);
        app.component('UiTabs', Tabs);
        app.component('UiTabPane', TabPane);
        app.component('UiTabNav', TabNav);
        app.component('UiLayout', Layout);
        app.component('UiContentContainer', ContentContainer);
    }
};