# 课设开发日志
## 开发日志
#### 3.12
-  用ai做了个简单框架
#### 3.19
-  完成删除图片功能，重命名功能，复制功能。
- 发现问题：
  - 1.左键框选图片不了
  - 2.在图片预览窗口不能右键也就不能粘贴
  - 3.右键窗口可多次重复出现，不能取消掉
  - 4.删除图片后图片预览窗口不能及时刷新，得手动在左侧树状目录单击刷新
  - 5.~~重命名不变名称重复后缀~~

## 课设题目
### 电子图片管理程序
#### 1. 基本要求
程序需要达到以下3个基本要求：
-  + [ ] 使用Java的图形用户界面设计程序界面，可以使用 JavaSwing 或 JavaFX 技术。
- + [ ] 程序能够显示的图片格式包括：.JPG、.JPEG、.GIF、.PNG、和.BMP。
- + [ ] 程序能够显示的图像文件尺寸，要求能够处理从几十K到几M的各种大小的图片。
程序主要由2个大的部分组成：**图片预览**和**幻灯片播放。**
下面以“美图看看”的界面为例说明需要每个部分需要完成的功能。美图看看的界面仅为参考，实现时可以自行设计，达到要求的功能即可。
#### 2. 图片预览部分

##### 2.1 图片预览部分窗口界面要求
图片预览容器至少包括如下3个区域：
- + [ ] 目录树区域，图1中上面左侧标出的部分，用于显示电脑的磁盘目录结构，注意只
显示目录，不要显示文件。当目录较多时应该显示滚动条。
- + [ ] 图片预览区域，图1中上面右侧标出的部分，用于以“缩略图”方式显示某个目录中
的所有图片文件。每个缩略图应该至少包括一个图片(原始图片的缩略图)和一个文
本(图片文件的名称)。图片预览区域中的缩略图应该保持原始图片的比例，不能扭
曲变形。当某个目录中的图片较多时应该显示滚动条。
- + [ ] 提示信息区域，图1中下面用红色粗线条标出部分，用来显示对图片进行操作过程中的提示信息。

##### 2.2 图片预览部分的基本功能要求
+ [ ] (1) 鼠标点击“目录树”中的某个目录时，在“图片预览”区域显示该目录中的所有图片，显示选中目录名称及有多少张图片(见图1中右侧上方蓝色线标出部分)，“提示信息”中显示图片个数及总的大小。
+ [ ] (2) 单张图片选择功能，使用鼠标左键点击“图片预览”中的一个缩略图时，该图片被选中。被选中的缩略图应该在外观上与其他图片区别，例如给被选中的缩略图加一个外框或使用不同的背影颜色，同时“提示信息”显示有几张图片被选中(见图1中左侧下方红色线标出部分)。
+ [ ] (3) 多张图片选择功能，使用鼠标或鼠标+键盘操作选中多张图片，例如给被选中的缩略图加一个外框或使用不同的背影颜色，同时“提示信息”显示有几张图片被选中(见图1中左侧下方红色线标出部分)。建议的多选操作方式(至少实现1种)：
- + [ ] 鼠标多选操作方式，在“图片预览”区域按住鼠标左键，拖动鼠标形成一个矩形区域，位于该矩形区域中缩略图被选中。
- + [ ] 鼠标+键盘多选操作方式，按住键盘的“ctrl”键，鼠标左键点击缩略图，在放开“ctrl”键之前所点击的图片全部被选中。

+ [ ] (4) 图片删除功能，使用“单选”或“多选”功能选中单张或多张图片后，删除选中的图片。注意删除之前要提示用户确认是否删除图片，同时更新“图片预览”区域。
+ [ ] (5) 图片复制功能，使用“单选”或“多选”功能选中单张或多张图片后，可以使用该功能复制选中图片，以备粘贴图片。
+ [ ] (6) 图片粘贴功能，使用图片复制功能复制了单张或多张图片后，可以使用粘贴图片功能将被复制的图片粘贴到当前选中目录，同时更新“图片预览”区域。注意当前选中目录可以是原图片目录也可以不是，粘贴时如果当前选中目录中存在相同名称的图片文件时，需要重新全名被粘贴的图片文件名称。
+ [ ] (7) 图片重命名功能，使用“单选”或“多选”功能选中图片后，使用本功能重新命名图片文件名称。注意，图片文件的扩展名不变，只重新命名主文件名称。
- + [ ] 单选状态下，要求用户直接输入新的文件名并按新名称命名图片
- + [ ] 多选状态下，要求用户输入名称前缀、起始编号、编号位数，例如:名称前缀为“NewName”、起始编号为“1”、编号位数为“4”。假设目录中共选中 67 个图片文件，则执行批量改名后，文件名为:NewName0001.JPG 、NewName0002.JPG、...、NewName0067.JPG。
+ [ ] (8) 取消选中状态，图片选中功能选中图片后，点击“图片预览”区域的空白区可以取消图片的选中状态。
+ [ ] (9) 右键菜单，图片的删除、复制、重命名功能，在使用选中功能之后，鼠标在被选中的图片上右击时，以鼠标右键菜单的形式展示。
说明：以上基本功能要求必须全部实现。各组可以根据自己的实际情况，增加其他功能。

####幻灯片播放部分
##### 幻灯片播放的界面参见下面的图2，进入幻灯片播放窗口的方式有2种：
- + [ ] 第1种方式是在图1中鼠标左键双击任意一个缩略图进入该窗口并显示双击的图片，参与播放的图片是与双击图片位于同一目录的所有图片。
- + [ ] 第2种方式是在图1中设计一个类似右下方红色圆标出的按钮，点击进入该窗口并显示当前图片预览目录中的第1张图，参与播放的图片是与双击图片位于同一目录的所有图片。
##### 幻灯片播放窗口界面说明
幻灯片播放窗口主要包括2个部分：
- + [ ] 图片展示区域，窗口的主体部分显示当前图片
- + [ ] 幻灯片播放操作功能栏，以按钮方式提供对幻灯片播放的操作
##### 3.2 幻灯片播放部分的基本功能要求
+ [ ] (1) 切换图片功能，鼠标点击功能栏“左”和“右”按钮，可以将显示的图片切换到当前预览目录中的前一张和后一张图片。当切换到目录的第1张或最后1张图片时应该有提示信息。
+ [ ] (2) 图片的放大缩小功能，鼠标点击功能栏中这“放大”和“缩小”按钮可以对图片进行放大和缩小。
+ [ ] (3) 幻灯片播放功能，在功能栏中设计一个“播放”按钮，点击该按钮可以从当前显示图片开始自动以确定的间隔时间(如1秒)切换显示图片。需要有退出播放功能。
说明：以上基本功能要求必须全部实现。各组可以根据自己的实际情况，增加其他功能。
